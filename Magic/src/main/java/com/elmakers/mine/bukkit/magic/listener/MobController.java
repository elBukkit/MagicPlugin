package com.elmakers.mine.bukkit.magic.listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.elmakers.mine.bukkit.api.event.MagicMobDeathEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityData;

public class MobController implements Listener {
    private MageController controller;
    private final Map<String, EntityData> mobs = new HashMap<>();
    private final Map<String, EntityData> mobsByName = new HashMap<>();

    public MobController(MageController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection configuration) {
        Set<String> mobKeys = configuration.getKeys(false);
        for (String mobKey : mobKeys) {
            ConfigurationSection mobConfiguration = configuration.getConfigurationSection(mobKey);
            if (!mobConfiguration.getBoolean("enabled", true)) continue;
            EntityData mob = new EntityData(controller, mobKey, mobConfiguration);
            mobs.put(mobKey, mob);

            String mobName = mob.getName();
            if (mobName != null && !mobName.isEmpty()) {
                mobsByName.put(mobName, mob);
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        String customName = entity.getCustomName();

        if (customName != null) {
            EntityData customMob = mobsByName.get(customName);
            if (customMob != null) {
                customMob.modify(controller, entity);
                event.setCancelled(false);
            }
        }
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
    public void onEntityTargetEnttiy(EntityTargetLivingEntityEvent event)
    {
        Entity source = event.getEntity();
        if (source instanceof Player || event.isCancelled()) return;

        Entity target = event.getTarget();
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

        for (Mage mage : magicMobs) {
            Entity entity = mage.getEntity();
            if (entity == null) continue;
            Location location = entity.getLocation();
            if (!chunk.getWorld().getName().equals(location.getWorld().getName())) continue;

            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            if (chunkZ != location.getBlockZ() >> 4 || chunkX != location.getBlockX() >> 4) continue;

            mage.sendDebugMessage(ChatColor.RED + "Despawned", 4);
            entity.remove();
        }
    }
}
