package com.elmakers.mine.bukkit.magic.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
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
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.event.MagicMobDeathEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

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

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Special check for mobs spawned internally
        if (EntityData.isSpawning) return;

        final Entity entity = event.getEntity();
        SpawnReason reason = event.getSpawnReason();
        if (reason != SpawnReason.SPAWNER
            && reason != SpawnReason.SPAWNER_EGG
            && reason != SpawnReason.DISPENSE_EGG
            && reason != SpawnReason.CUSTOM) {
            return;
        }

        Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                String customName = entity.getCustomName();
                if (customName != null) {
                    EntityData customMob = mobsByName.get(customName);
                    if (customMob != null) {
                        customMob.modify(controller, entity);
                    }
                } else if (entity.getType() != EntityType.PLAYER) {
                    EntityData customMob = defaultMobs.get(entity.getType());
                    if (customMob != null) {
                        customMob.modify(controller, entity);
                    }
                }
            }
        }, 1);
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
            if (source.hasMetadata("owner")) {
                List<MetadataValue> metadata = source.getMetadata("owner");
                for (MetadataValue value : metadata) {
                    String ownerId = value.asString();
                    Mage mageOwner = controller.getRegisteredMage(ownerId);
                    if (mageOwner != null && mageOwner.getEntity() == target) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            Mage targetMage = controller.getRegisteredMage(target);
            if (targetMage != null && targetMage.isSuperProtected()) {
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
        if (entity.hasMetadata("nosplit")) {
            event.setCancelled(true);
            entity.removeMetadata("nosplit", controller.getPlugin());
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
            mob = defaultMobs.get(entity.getType());
        }
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
            entity.setMetadata("nosplit", new FixedMetadataValue(controller.getPlugin(), true));
        }
        if (!entity.hasMetadata("nodrops")) {
            mob.modifyDrops(controller, event);
        }
        entity.removeMetadata("nodrops", controller.getPlugin());
    }

    public int getCount() {
        return mobs.size();
    }

    public Set<String> getKeys() {
        return mobs.keySet();
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

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        Collection<Mage> magicMobs = controller.getMobMages();
        List<Entity> toRemove = null;

        for (Mage mage : magicMobs) {
            Entity entity = mage.getEntity();
            if (entity == null) continue;
            Location location = entity.getLocation();
            if (!chunk.getWorld().getName().equals(location.getWorld().getName())) continue;

            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            if (chunkZ != location.getBlockZ() >> 4 || chunkX != location.getBlockX() >> 4) continue;

            mage.sendDebugMessage(ChatColor.RED + "Despawned", 4);

            if (toRemove == null) {
                toRemove = new ArrayList<>();
            }
            toRemove.add(entity);
        }

        // Someone on DBO reported getting a CME when entities were removed inline.
        // I can't really see how that could happen, but I put a fix in anyway.
        if (toRemove != null) {
            for (Entity entity : toRemove) {
                entity.remove();
            }
        }
    }
}
