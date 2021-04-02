package com.elmakers.mine.bukkit.magic.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.elmakers.mine.bukkit.api.event.MagicMobDeathEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.tasks.CheckChunkTask;
import com.elmakers.mine.bukkit.tasks.ModifyEntityTask;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.EntityMetadataUtils;

public class MobController implements Listener, ChunkLoadListener {
    public static boolean REMOVE_INVULNERABLE = false;
    private MagicController controller;
    private final Map<String, EntityData> mobs = new HashMap<>();
    private final Map<String, EntityData> mobsByName = new HashMap<>();
    private final Map<EntityType, EntityData> defaultMobs = new HashMap<>();
    private final Map<Entity, EntityData> activeMobs = new WeakHashMap<>();

    public MobController(MagicController controller) {
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

    public void checkMagicMob(Entity entity, String mobKey) {
        com.elmakers.mine.bukkit.api.entity.EntityData storedMob = controller.getMob(mobKey);
        if (storedMob != null) {
            storedMob.modify(entity);
        }
    }

    public void checkNPC(Entity entity, String npcId) {
        try {
            MagicNPC npc = controller.getNPC(npcId);
            if (npc == null || !npc.isEntity(entity)) {
                Location location = entity.getLocation();
                controller.getLogger().warning("Removing an invalid NPC (id=" + npcId + ") entity of type " + entity.getType() + " at ["
                    + location.getWorld().getName() + "] " + location.getBlockX()
                    + "," + location.getBlockY() + "," + location.getBlockZ());
                entity.remove();
            }
            // TODO: This would also be a better way to reactivate NPCs, but we can't rely on this until
            // versions of spigot without the persistent metadata API are no longer supported
        } catch (Exception ex) {
            controller.getLogger().log(Level.SEVERE, "Error reading entity NPC id", ex);
        }
    }

    @Override
    public void onChunkLoad(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            String magicMobKey = EntityMetadataUtils.instance().getString(entity, "magicmob");
            if (magicMobKey != null) {
                checkMagicMob(entity, magicMobKey);
            }
            // Check for disconnected NPCs, we don't want to leave invulnerable entities around
            String npcId = EntityMetadataUtils.instance().getString(entity, "npc_id");
            if (npcId != null) {
                checkNPC(entity, npcId);
            } else if (REMOVE_INVULNERABLE && entity.getType() != EntityType.DROPPED_ITEM
                && CompatibilityUtils.isInvulnerable(entity)) {
                // Don't remove invulnerable items since those could be dropped wands
                Location location = entity.getLocation();
                controller.getLogger().warning("Removing an invulnerable entity of type " + entity.getType() + " at ["
                    + location.getWorld().getName() + "] " + location.getBlockX()
                    + "," + location.getBlockY() + "," + location.getBlockZ());
                entity.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        if (!controller.isDataLoaded()) {
            CheckChunkTask.defer(controller.getPlugin(), this, chunk);
        } else {
            onChunkLoad(chunk);
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

            com.elmakers.mine.bukkit.api.entity.EntityData entityData = controller.getMob(source);
            if (entityData != null) {
                // Docile handled above in onEntityTarget
                if (!entityData.canTarget(target)) {
                    event.setCancelled(true);
                }
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
        if (mob == null) {
            return;
        }

        if (entity instanceof Player) {
            controller.getLogger().warning("A player has magic mob data on death, this shouldn't happen");
            return;
        }

        // Prevent processing double-death events
        activeMobs.remove(entity);
        MagicMobDeathEvent deathEvent = new MagicMobDeathEvent(controller, mob, event);
        Bukkit.getPluginManager().callEvent(deathEvent);

        mob.onDeath(entity);
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

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        if (controller.isDespawnMagicMobs()) {
            Collection<Mage> magicMobs = controller.getMobMages();
            for (Mage mage : magicMobs) {
                Entity entity = mage.getEntity();
                if (entity == null) continue;
                Location location = entity.getLocation();
                if (chunk.getWorld() != location.getWorld()) continue;

                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();
                if (chunkZ != location.getBlockZ() >> 4 || chunkX != location.getBlockX() >> 4) continue;

                mage.sendDebugMessage(ChatColor.RED + "Despawned", 4);
                entity.remove();
            }
        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        EntityData entityData = getEntityData(event.getEntity());
        if (entityData != null && entityData.isPreventDismount()) {
            event.setCancelled(true);
        }
    }
}
