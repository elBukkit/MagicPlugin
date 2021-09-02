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
import org.bukkit.event.entity.EntityTeleportEvent;
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
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.tasks.CheckChunkTask;
import com.elmakers.mine.bukkit.tasks.CheckEntitySpawnTask;
import com.elmakers.mine.bukkit.tasks.ModifyEntityTask;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

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
        if (mobName != null && !mobName.isEmpty() && mob.isRegisterByName()) {
            mobsByName.put(mobName, mob);
        }
    }

    public void registerMythicMobs(Collection<String> mythicMobKeys) {
        for (String mythicMobKey : mythicMobKeys) {
            EntityData existing = mobs.get(mythicMobKey);
            if (existing != null) {
                existing.setMythicMobKey(mythicMobKey);
                continue;
            }

            EntityData wrapper = EntityData.wrapMythicMob(controller, mythicMobKey);
            mobs.put(mythicMobKey, wrapper);
        }
    }

    public void validate() {
        for (EntityData mob : mobs.values()) {
            mob.validate();
        }
    }

    public void checkMagicMob(Entity entity, String mobKey) {
        com.elmakers.mine.bukkit.api.entity.EntityData storedMob = controller.getMob(mobKey);
        if (storedMob != null) {
            storedMob.modify(entity);
        }
    }

    public void updateAllMobs() {
        // Not clearing the map, but hopefully everything in it will be replaced
        Map<Entity, EntityData> currentMobs = new HashMap<>(activeMobs);
        for (Map.Entry<Entity, EntityData> entry : currentMobs.entrySet()) {
            EntityData mob = entry.getValue();
            String key = mob.getKey();
            if (key == null || key.isEmpty()) continue;
            mob = controller.getMob(key);
            if (mob != null) {
                mob.modify(entry.getKey());
            }
        }
    }

    public void checkNPC(Entity entity, String npcId) {
        try {
            MagicNPC npc = controller.getNPC(npcId);
            if (npc == null || !npc.isEntity(entity)) {
                Location location = entity.getLocation();
                controller.info("Removing an invalid NPC (id=" + npcId + ") entity of type " + entity.getType() + " at ["
                    + location.getWorld().getName() + "] " + location.getBlockX()
                    + "," + location.getBlockY() + "," + location.getBlockZ(), 5);
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
            // Check for disconnected NPCs, we don't want to leave invulnerable entities around
            boolean removed = false;
            String npcId = CompatibilityLib.getEntityMetadataUtils().getString(entity, MagicMetaKeys.NPC_ID);
            if (npcId != null) {
                checkNPC(entity, npcId);
            } else if (REMOVE_INVULNERABLE && entity.getType() != EntityType.DROPPED_ITEM
                && CompatibilityLib.getCompatibilityUtils().isInvulnerable(entity)) {
                // Don't remove invulnerable items since those could be dropped wands
                Location location = entity.getLocation();
                controller.getLogger().warning("Removing an invulnerable entity of type " + entity.getType() + " at ["
                    + location.getWorld().getName() + "] " + location.getBlockX()
                    + "," + location.getBlockY() + "," + location.getBlockZ());
                entity.remove();
                removed = true;
            }

            // If it's not an NPC and we didn't remove it, check for Magic Mob data
            if (!removed && npcId == null) {
                String magicMobKey = CompatibilityLib.getEntityMetadataUtils().getString(entity, MagicMetaKeys.MAGIC_MOB);
                if (magicMobKey != null) {
                    checkMagicMob(entity, magicMobKey);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {
        CheckChunkTask.process(controller, this, event.getChunk());
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

        // We can process natural spawns now because we assume the mobs are complete.
        // Custom spawns need to be deferred one tick to give MythicMobs (or other plugins)
        // a chance to register the mob and set its name.
        Plugin plugin = controller.getPlugin();
        SpawnReason reason = event.getSpawnReason();
        if (reason == SpawnReason.CUSTOM) {
            plugin.getServer().getScheduler().runTaskLater(plugin, new CheckEntitySpawnTask(this, entity), 1);
            return;
        }

        // Natural spawns should only check for default mobs
        if (checkDefaultSpawn(entity, true)) {
            return;
        }

        // Spawners and spawn eggs can be processed now since they will already have their display name
        if (reason != SpawnReason.SPAWNER
                && reason != SpawnReason.SPAWNER_EGG
                && reason != SpawnReason.DISPENSE_EGG) {
            return;
        }

        checkEntitySpawn(entity, true);
    }

    public boolean checkDefaultSpawn(Entity entity, boolean defer) {
        // Check for default mob overrides
        String customName = entity.getCustomName();
        EntityData customMob = defaultMobs.get(entity.getType());
        if (customName == null && customMob != null) {
            if (defer) {
                Plugin plugin = controller.getPlugin();
                plugin.getServer().getScheduler().runTaskLater(plugin, new ModifyEntityTask(controller, customMob, entity), 1);
            } else {
                customMob.modify(entity);
            }
            return true;
        }
        return false;
    }

    public boolean checkEntitySpawn(Entity entity, boolean defer) {
        // Check for mythic mob spawns
        String mythicMobKey = controller.getMythicMobKey(entity);
        if (mythicMobKey != null) {
            com.elmakers.mine.bukkit.api.entity.EntityData mythicMob = controller.getMob(mythicMobKey);
            if (mythicMob != null) {
                mythicMob.modify(entity);
                return true;
            }
        }

        // Check for named custom mobs
        String customName = entity.getCustomName();
        if (customName == null || customName.isEmpty()) {
            return false;
        }

        // This is to allow attaching data to mobs spawned by spawners, eggs or other plugins
        final com.elmakers.mine.bukkit.entity.EntityData namedMob = mobsByName.get(customName);
        if (namedMob == null) {
            return false;
        }

        if (defer) {
            Plugin plugin = controller.getPlugin();
            plugin.getServer().getScheduler().runTaskLater(plugin, new ModifyEntityTask(controller, namedMob, entity), 1);
        } else {
            namedMob.modify(entity);
        }
        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        if (controller.isMagicNPC(entity)) {
            event.setCancelled(true);
        } else if (event.getReason() != EntityTargetEvent.TargetReason.CUSTOM && event.getReason() != EntityTargetEvent.TargetReason.UNKNOWN) {
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
            String ownerId = CompatibilityLib.getEntityMetadataUtils().getString(source, MagicMetaKeys.OWNER);
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

        if (target == null) {
            com.elmakers.mine.bukkit.api.entity.EntityData mageMob = mage.getEntityData();
            if (mageMob != null && !mageMob.isRelentless()) {
                return;
            }
        }
        Entity currentTarget = mage.getTopDamager();
        if (currentTarget != null && currentTarget != target) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent event) {
        Entity entity = event.getEntity();
        if (CompatibilityLib.getEntityMetadataUtils().getBoolean(entity, MagicMetaKeys.NOSPLIT)) {
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
            CompatibilityLib.getEntityMetadataUtils().setBoolean(entity, MagicMetaKeys.NOSPLIT, true);
        }
        if (!CompatibilityLib.getEntityMetadataUtils().getBoolean(entity, MagicMetaKeys.NO_DROPS)) {
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
        return activeMobs.get(entity);
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
        if (CompatibilityLib.getCompatibilityUtils().isTeleporting()) return;

        EntityData entityData = getEntityData(event.getEntity());
        if (entityData != null && entityData.isPreventDismount()) {
            CompatibilityLib.getCompatibilityUtils().cancelDismount(event);
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        EntityData entityData = getEntityData(event.getEntity());
        if (entityData != null && entityData.isPreventTeleport()) {
            event.setCancelled(true);
        }
    }
}
