package com.elmakers.mine.bukkit.magic.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.event.MagicMobDeathEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityData;

public class MobController implements Listener {
    private MageController controller;
    private final Map<String, EntityData> mobs = new HashMap<>();
    private final Map<String, EntityData> mobsByName = new HashMap<>();
    private final Map<EntityType, EntityData> defaultMobs = new HashMap<>();

    public MobController(MageController controller) {
        this.controller = controller;
    }

    public void clear() {
        mobs.clear();
        mobsByName.clear();
    }

    public void load(String mobKey, ConfigurationSection mobConfiguration) {
        if (!mobConfiguration.getBoolean("enabled", true)) {
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTarget(EntityTargetEvent event) {
        // TODO: Don't use metadata!
        if (event.isCancelled() || !event.getEntity().hasMetadata("docile")) {
            return;
        }

        if (event.getReason() == EntityTargetEvent.TargetReason.CLOSEST_PLAYER) {
            event.setCancelled(true);
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity))
        {
            return;
        }

        LivingEntity died = (LivingEntity)entity;
        String name = died.getCustomName();
        if (name == null || name.isEmpty())
        {
            EntityData mob = defaultMobs.get(died.getType());
            if (mob != null && !died.hasMetadata("nodrops")) {
                mob.modifyDrops(controller, event);
            }
            return;
        }

        EntityData mob = mobsByName.get(name);
        if (mob == null) return;

        MagicMobDeathEvent deathEvent = new MagicMobDeathEvent(controller, mob, event);
        Bukkit.getPluginManager().callEvent(deathEvent);

        if (!died.hasMetadata("nodrops")) {
            mob.modifyDrops(controller, event);
        }

        // Prevent double-deaths .. gg Mojang?
        // Kind of hacky to use this flag for it, but seemed easiest
        died.setCustomNameVisible(false);
        died.setCustomName(null);
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
