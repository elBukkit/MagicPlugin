package com.elmakers.mine.bukkit.block.magic;

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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Locatable;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.effect.EffectContext;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MagicBlock implements Locatable, com.elmakers.mine.bukkit.api.automata.Automaton,
        com.elmakers.mine.bukkit.api.block.magic.MagicBlock {
    @Nonnull
    private final MagicController controller;
    @Nullable
    private MagicBlockTemplate template;
    @Nullable
    private ConfigurationSection parameters;
    private String templateKey;
    @Nonnull
    private Location location;
    private String worldName;
    private long createdAt;
    private String creatorId;
    private String creatorName;
    private String name;

    private long nextTick;
    private List<WeakReference<Entity>> spawned;
    private long lastSpawn;
    private EffectContext effectContext;
    private boolean isActive;
    private boolean enabled = true;

    private Mage mage;

    public MagicBlock(@Nonnull MagicController controller, @Nonnull ConfigurationSection node) {
        this.controller = controller;
        enabled = node.getBoolean("enabled", true);
        templateKey = node.getString("template");
        parameters = ConfigurationUtils.getConfigurationSection(node, "parameters");
        if (templateKey != null) {
            setTemplate(controller.getMagicBlockTemplate(templateKey));
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
        worldName = node.getString("world");
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

    public MagicBlock(@Nonnull MagicController controller, @Nonnull Location location, @Nonnull String templateKey, String creatorId, String creatorName, @Nullable ConfigurationSection parameters) {
        this.controller = controller;
        this.templateKey = templateKey;
        this.parameters = parameters;
        this.location = location;
        World world = this.location.getWorld();
        worldName = world == null ? null : world.getName();
        setTemplate(controller.getMagicBlockTemplate(templateKey));
        createdAt = System.currentTimeMillis();
        this.creatorId = creatorId;
        this.creatorName = creatorName;
    }

    private void setTemplate(MagicBlockTemplate template) {
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
            setTemplate(controller.getMagicBlockTemplate(template.getKey()));
        }
    }

    public void save(ConfigurationSection node) {
        node.set("enabled", enabled);
        node.set("created", createdAt);
        node.set("creator", creatorId);
        node.set("creator_name", creatorName);
        node.set("template", templateKey);
        node.set("world", worldName);
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

    @Override
    public void pause() {
        deactivate();
    }

    @Override
    public void resume() {
        if (template == null) return;

        // Always tick at least once
        tick();
    }

    public void activate() {
        isActive = true;

        if (template != null) {
            Collection<EffectPlayer> effects = template.getEffects();
            if (effects != null) {
                for (EffectPlayer player : effects) {
                    player.start(getEffectContext());
                }
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
            spawned.clear();
        }
        lastSpawn = 0;

        if (effectContext != null) {
            effectContext.cancelEffects();
            effectContext = null;
        }
        if (template != null) {
            Collection<EffectPlayer> effects = template.getEffects();
            if (effects != null) {
                for (EffectPlayer player : effects) {
                    player.cancel();
                }
            }
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
            this.mage = null;
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        if (enabled) return;
        this.enabled = true;
        if (inActiveChunk()) {
            resume();
        }
    }

    public void disable() {
        this.enabled = false;
        pause();
    }

    public boolean inActiveChunk() {
        return CompatibilityLib.getCompatibilityUtils().isChunkLoaded(getLocation()) || isAlwaysActive();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        World world = this.location.getWorld();
        worldName = world == null ? null : world.getName();
    }

    public void track(List<Entity> entities) {
        if (template == null) return;
        Spawner spawner = template.getSpawner();
        if (spawner == null || !spawner.isTracked()) return;
        if (spawned == null) {
            spawned = new ArrayList<>();
        }
        for (Entity entity : entities) {
            CompatibilityLib.getEntityMetadataUtils().setLong(entity, MagicMetaKeys.AUTOMATION, getId());
            spawned.add(new WeakReference<>(entity));
        }
    }

    public void checkEntities() {
        if (spawned == null || template == null) return;
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
            } else if (leashRangeSquared > 0) {
                if (mob.getLocation().distanceSquared(location) > leashRangeSquared) {
                    mob.teleport(spawner.getSpawnLocation(location));
                }
            }
        }
    }

    public void onSpawnDeath() {
        lastSpawn = System.currentTimeMillis();
    }

    public void spawn() {
        if (template == null) return;
        Spawner spawner = template.getSpawner();
        if (spawner == null) return;
        List<Entity> entities = spawner.spawn(this);
        if (entities != null && !entities.isEmpty()) {
            lastSpawn = System.currentTimeMillis();
            track(entities);
        }
    }

    public void tick() {
        if (template == null || !enabled) return;

        long now = System.currentTimeMillis();
        if (now < nextTick) return;
        template.tick(this);
        nextTick = now + template.getInterval();
    }

    public boolean hasSpawner() {
        return template != null && template.getSpawner() != null;
    }

    public long getTimeToNextSpawn() {
        if (template == null) return 0;
        Spawner spawner = template.getSpawner();
        if (spawner == null) return 0;
        int spawnInterval = spawner.getInterval();
        if (spawnInterval == 0) return 0;
        return Math.max(0, lastSpawn + spawnInterval - System.currentTimeMillis());
    }

    public int getSpawnLimit() {
        if (template == null) return 0;
        Spawner spawner = template.getSpawner();
        return spawner == null ? 0 : spawner.getLimit();
    }

    public int getSpawnMinPlayers() {
        if (template == null) return 0;
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
    public MagicBlockTemplate getTemplate() {
        return template;
    }

    public boolean isAlwaysActive() {
        return template != null && template.isAlwaysActive();
    }

    public boolean removeWhenBroken() {
        return template != null && template.removeWhenBroken();
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
            mage = controller.getBlockMage(automatonId, template == null ? "?" : template.getName());
            mage.setLocation(location);
        }

        return mage;
    }

    public int getSpawnedCount() {
        return spawned == null ? 0 : spawned.size();
    }

    @Nullable
    public Nearby getNearby() {
        if (template == null) return null;
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

    public void removed() {
        if (template == null) return;
        if (template.removeWhenBroken() && location != null) {
            location.getBlock().setType(Material.AIR);
        }
        String dropWhenRemoved = template.getDropWhenRemoved();
        if (dropWhenRemoved != null && !dropWhenRemoved.isEmpty() && location != null) {
            ItemData item = controller.getOrCreateItem(dropWhenRemoved);
            ItemStack stack = item == null ? null : item.getItemStack();
            if (CompatibilityLib.getItemUtils().isEmpty(stack)) {
                controller.getLogger().warning("Invalid item dropped in automaton " + template.getKey() + ": " + dropWhenRemoved);
            } else {
                location.getWorld().dropItemNaturally(location, stack);
            }
        }
    }
}
