package com.elmakers.mine.bukkit.automata;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Locatable;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.effect.EffectContext;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class Automaton implements Locatable {
    @Nonnull
    private final MagicController controller;
    @Nullable
    private AutomatonTemplate template;
    @Nullable
    private ConfigurationSection parameters;
    private String templateKey;
    @Nonnull
    private final Location location;
    private long createdAt;
    private String creatorId;
    private String creatorName;
    private String name;

    private long nextTick;
    private List<WeakReference<Entity>> spawned;
    private long lastSpawn;
    private EffectContext effectContext;
    private boolean isActive;

    private Mage mage;

    public Automaton(@Nonnull MagicController controller, @Nonnull ConfigurationSection node) {
        this.controller = controller;
        templateKey = node.getString("template");
        parameters = ConfigurationUtils.getConfigurationSection(node, "parameters");
        if (templateKey != null) {
            setTemplate(controller.getAutomatonTemplate(templateKey));
        }
        if (template == null) {
            controller.getLogger().warning("Automaton missing template: " + templateKey);
        }
        createdAt = node.getLong("created", 0);
        creatorId = node.getString("creator");
        creatorName = node.getString("creator_name");
        name = node.getString("name");

        double x = node.getDouble("x");
        double y = node.getDouble("y");
        double z = node.getDouble("z");
        float yaw = (float)node.getDouble("yaw");
        float pitch = (float)node.getDouble("pitch");
        String worldName = node.getString("world");
        if (worldName == null || worldName.isEmpty()) {
            worldName = "world";
            controller.getLogger().warning("Automaton missing world name, defaulting to 'world'");
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            controller.getLogger().warning("Automaton has unknown world: " + worldName + ", will be removed!");
        }
        location = new Location(world, x, y, z, yaw, pitch);
    }

    public Automaton(@Nonnull MagicController controller, @Nonnull Location location, @Nonnull String templateKey, String creatorId, String creatorName, @Nullable ConfigurationSection parameters) {
        this.controller = controller;
        this.templateKey = templateKey;
        this.parameters = parameters;
        this.location = location;
        setTemplate(controller.getAutomatonTemplate(templateKey));
        createdAt = System.currentTimeMillis();
        this.creatorId = creatorId;
        this.creatorName = creatorName;
    }

    private void setTemplate(AutomatonTemplate template) {
        this.template = template;
        if (template != null) {
            if (parameters != null) {
                this.template = template.getVariant(parameters);
            }
            nextTick = 0;
            lastSpawn = 0;
        }
    }

    public void reload() {
        if (template != null) {
            setTemplate(controller.getAutomatonTemplate(template.getKey()));
        }
    }

    public void save(ConfigurationSection node) {
        node.set("created", createdAt);
        node.set("creator", creatorId);
        node.set("creator_name", creatorName);
        node.set("template", templateKey);
        node.set("world", location.getWorld().getName());
        node.set("x", location.getX());
        node.set("y", location.getY());
        node.set("z", location.getZ());
        node.set("yaw", location.getYaw());
        node.set("pitch", location.getPitch());
        node.set("parameters", parameters);
        node.set("name", name);
    }

    public long getCreatedTime() {
        return createdAt;
    }

    public void pause() {
        deactivate();
    }

    public void resume() {
        if (template == null) return;

        // Always tick at least once
        tick();
    }

    public void activate() {
        isActive = true;

        Collection<EffectPlayer> effects = template.getEffects();
        if (effects != null) {
            for (EffectPlayer player : effects) {
                player.start(getEffectContext());
            }
        }
    }

    public void deactivate() {
        isActive = false;

        if (spawned != null) {
            for (WeakReference<Entity> mobReference : spawned) {
                Entity mob = mobReference.get();
                if (mob != null && mob.isValid()) {
                    mob.remove();
                }
            }
        }
        lastSpawn = 0;

        if (effectContext != null) {
            effectContext.cancelEffects();
            effectContext = null;
        }

        if (mage != null) {
            Mage mage = this.mage;
            mage.deactivate();
            mage.undoScheduled();
            if (template != null && template.isUndoAll()) {
                UndoList undone = mage.undo();
                while (undone != null) {
                    undone = mage.undo();
                }
            }
            controller.forgetMage(mage);
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void track(List<Entity> entities) {
        if (spawned == null) {
            spawned = new ArrayList<>();
        }
        for (Entity entity : entities) {
            spawned.add(new WeakReference<>(entity));
        }
    }

    public void checkEntities() {
        if (spawned == null) return;
        Spawner spawner = template.getSpawner();
        double leashRangeSquared = spawner == null || !spawner.isLeashed() ? 0 : spawner.getLimitRange();
        if (leashRangeSquared > 0) {
            leashRangeSquared = leashRangeSquared * leashRangeSquared;
        }
        Iterator<WeakReference<Entity>> iterator = spawned.iterator();
        while (iterator.hasNext()) {
            WeakReference<Entity> mobReference = iterator.next();
            Entity mob = mobReference.get();
            if (mob == null || !mob.isValid()) {
                iterator.remove();
                lastSpawn = System.currentTimeMillis();
            } else if (leashRangeSquared > 0) {
                if (mob.getLocation().distanceSquared(location) > leashRangeSquared) {
                    mob.teleport(spawner.getSpawnLocation(location));
                }
            }
        }
    }

    public void spawn() {
        Spawner spawner = template.getSpawner();
        if (spawner == null) return;
        List<Entity> entities = spawner.spawn(this);
        if (entities != null && !entities.isEmpty()) {
            lastSpawn = System.currentTimeMillis();
            track(entities);
        }
    }

    public void tick() {
        if (template == null) return;

        long now = System.currentTimeMillis();
        if (now < nextTick) return;
        template.tick(this);
        nextTick = now + template.getInterval();
    }

    public boolean hasSpawner() {
        return template.getSpawner() != null;
    }

    public long getTimeToNextSpawn() {
        Spawner spawner = template.getSpawner();
        if (spawner == null) return 0;
        int spawnInterval = spawner.getInterval();
        if (spawnInterval == 0) return 0;
        return Math.max(0, lastSpawn + spawnInterval - System.currentTimeMillis());
    }

    public int getSpawnLimit() {
        Spawner spawner = template.getSpawner();
        return spawner == null ? 0 : spawner.getLimit();
    }

    public int getSpawnMinPlayers() {
        Spawner spawner = template.getSpawner();
        return spawner == null ? 0 : spawner.getMinPlayers();
    }

    public long getId() {
        return BlockData.getBlockId(getLocation());
    }

    public boolean isValid() {
        return location.getWorld() != null;
    }

    @Nonnull
    public String getTemplateKey() {
        return templateKey;
    }

    @Nullable
    public AutomatonTemplate getTemplate() {
        return template;
    }


    @Nonnull
    private EffectContext getEffectContext() {
        if (effectContext == null) {
            effectContext = new EffectContext(controller, location);
        }
        return effectContext;
    }

    @Nullable
    public ConfigurationSection getParameters() {
        return parameters;
    }

    public void setParameters(@Nullable ConfigurationSection parameters) {
        this.parameters = parameters;
    }

    @Nullable
    public String getCreatorName() {
        return creatorName;
    }

    @Nonnull
    public Mage getMage() {
        if (mage == null) {
            String automatonId = UUID.randomUUID().toString();
            mage = controller.getAutomaton(automatonId, template.getName());
            mage.setLocation(location);
        }

        return mage;
    }

    public int getSpawnedCount() {
        return spawned == null ? 0 : spawned.size();
    }

    @Nullable
    public Nearby getNearby() {
        Spawner spawner = template.getSpawner();
        Nearby nearby = null;
        if (spawner != null) {
            nearby = spawner.getNearby(this);
            if (nearby != null && spawner.isTracked()) {
                nearby.mobs = spawned == null ? 0 : spawned.size();
            }
        }
        return nearby;
    }

    @Nullable
    public Collection<WeakReference<Entity>> getSpawned() {
        return spawned;
    }

    @Override
    @Nonnull
    public String getName() {
        return name == null ? (template == null ? "(Unknown)" : template.getName()) : name;
    }

    @Nullable
    public String getDescription() {
        return template == null ? null : template.getDescription();
    }

    public void setName(String name) {
        this.name = name;
    }
}
