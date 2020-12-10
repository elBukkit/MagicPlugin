package com.elmakers.mine.bukkit.magic.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.event.MagicMobDeathEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.tasks.ModifyEntityTask;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.EntityMetadataUtils;

public class MobController implements Listener {
    private MageController controller;
    private final Map<String, EntityData> mobs = new HashMap<>();
    private final Map<String, EntityData> mobsByName = new HashMap<>();
    private final Map<EntityType, EntityData> defaultMobs = new HashMap<>();
    private final Map<Entity, EntityData> activeMobs = new WeakHashMap<>();

    public MobController(MageController controller) {
        this.controller = controller;
    }

    public void clear() {
        mobs.clear();
        mobsByName.clear();
    }

    public void load(String mobKey, ConfigurationSection mobConfiguration) {
        if (!ConfigurationUtils.isEnabled(mobConfiguration)) {
            return;
        }
        EntityData mob = new EntityData(controller, mobKey, mobConfiguration);
        try {
            EntityType defaultType = EntityType.valueOf(mobKey.toUpperCase());
            defaultMobs.put(defaultType, mob);
            return;
        } catch (Exception ignore) {
        }

        mobs.put(mobKey, mob);

        String mobName = mob.getName();
        if (mobName != null && !mobName.isEmpty()) {
            mobsByName.put(mobName, mob);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            String magicMobKey = EntityMetadataUtils.instance().getString(entity, "magicmob");
            com.elmakers.mine.bukkit.api.entity.EntityData storedMob = controller.getMob(magicMobKey);
            if (storedMob != null) {
                storedMob.modify(entity);
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Special check for mobs spawned internally
        // These should already have their mob data assigned
        if (EntityData.isSpawning) return;

        // Ignore players spawns
        final Entity entity = event.getEntity();
        if (entity.getType() == EntityType.PLAYER) {
            return;
        }

        // Check for default mob overrides
        Plugin plugin = controller.getPlugin();
        String customName = entity.getCustomName();
        EntityData customMob = defaultMobs.get(entity.getType());
        if (customName == null && customMob != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, new ModifyEntityTask(controller, customMob, entity), 1);
            return;
        }

        // Now only named mobs will be tagged
        if (customName == null || customName.isEmpty()) {
            return;
        }

        // Only want non-natural spawns here
        // We assume mobs won't naturally spawn with a name.
        SpawnReason reason = event.getSpawnReason();
        if (reason != SpawnReason.SPAWNER
            && reason != SpawnReason.SPAWNER_EGG
            && reason != SpawnReason.DISPENSE_EGG
            && reason != SpawnReason.CUSTOM) {
            return;
        }

        // Check for named custom mobs
        // This is to allow attaching data to mobs spawned by spawners, eggs or other plugins
        final EntityData namedMob = mobsByName.get(customName);
        if (namedMob == null) {
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, new ModifyEntityTask(controller, namedMob, entity), 1);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        if (controller.isMagicNPC(entity)) {
            event.setCancelled(true);
        } else if (event.getReason() == EntityTargetEvent.TargetReason.CLOSEST_PLAYER) {
            com.elmakers.mine.bukkit.api.entity.EntityData entityData = controller.getMob(entity);
            if (entityData != null && entityData.isDocile()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTargetEntity(EntityTargetLivingEntityEvent event)
    {
        Entity source = event.getEntity();
        if (source instanceof Player || event.isCancelled()) return;

        Entity target = event.getTarget();
        if (target != null) {
            String ownerId = EntityMetadataUtils.instance().getString(source, "owner");
            if (ownerId != null) {
                Mage mageOwner = controller.getRegisteredMage(ownerId);
                if (mageOwner != null && mageOwner.getEntity() == target) {
                    event.setCancelled(true);
                    return;
                }
            }

            Mage targetMage = controller.getRegisteredMage(target);
            if (targetMage != null && targetMage.isIgnoredByMobs()) {
                event.setCancelled(true);
                return;
            }
        }

        Mage mage = controller.getRegisteredMage(source);
        if (mage == null) return;

        Entity currentTarget = mage.getTopDamager();
        if (currentTarget != null && currentTarget != target) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent event) {
        Entity entity = event.getEntity();
        if (EntityMetadataUtils.instance().getBoolean(entity, "nosplit")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        EntityData mob = activeMobs.get(entity);
        boolean isMagicMob = mob != null;
        if (mob == null) {
            return;
        }

        // Prevent processing double-death events
        if (isMagicMob) {
            activeMobs.remove(entity);
            MagicMobDeathEvent deathEvent = new MagicMobDeathEvent(controller, mob, event);
            Bukkit.getPluginManager().callEvent(deathEvent);
        }
        if (!mob.isSplittable()) {
            EntityMetadataUtils.instance().setBoolean(entity, "nosplit", true);
        }
        if (!EntityMetadataUtils.instance().getBoolean(entity, "nodrops")) {
            mob.modifyDrops(event);
        }
    }

    public int getCount() {
        return mobs.size();
    }

    public Set<String> getKeys() {
        return mobs.keySet();
    }

    public Collection<EntityData> getMobs() {
        return mobs.values();
    }

    public EntityData get(String key) {
        return mobs.get(key);
    }

    public EntityData getByName(String name) {
        return mobsByName.get(name);
    }

    public void register(@Nonnull Entity entity, @Nonnull EntityData entityData) {
        activeMobs.put(entity, entityData);
    }

    @Nullable
    public EntityData getEntityData(Entity entity) {
        EntityData entityData = activeMobs.get(entity);
        if (entityData == null) {
            String customName = entity.getCustomName();
            if (customName != null) {
                entityData = mobsByName.get(customName);
            }
            if (entityData == null) {
                entityData = defaultMobs.get(entity.getType());
            }
        }
        return entityData;
    }

    @Nonnull
    public Collection<Entity> getActiveMobs() {
        return new ArrayList<>(activeMobs.keySet());
    }

    @Nonnull
    public EntityData getDefaultMob(EntityType entityType) {
        EntityData defaultMob = defaultMobs.get(entityType);
        if (defaultMob == null) {
            defaultMob = new com.elmakers.mine.bukkit.entity.EntityData(controller, entityType);
        }
        return defaultMob;
    }
}
