package com.elmakers.mine.bukkit.world.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.WorldController;

public class WorldSpawnListener implements Listener
{
    private final WorldController controller;
    private boolean spawning = false;
    private Set<SpawnReason> ignoreReasons = new HashSet<>();

    public WorldSpawnListener(final WorldController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection config) {
        List<String> reasonList = ConfigurationUtils.getStringList(config, "ignore_reasons");
        ignoreReasons.clear();
        if (reasonList != null) {
            for (String reason : reasonList) {
                try {
                    SpawnReason ignoreReason = SpawnReason.valueOf(reason.toUpperCase());
                    ignoreReasons.add(ignoreReason);
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid spawn reason in ignore_reasons: " + reason);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (spawning || ignoreReasons.contains(event.getSpawnReason())) return;

        MagicWorld magicWorld = controller.getWorld(event.getLocation().getWorld().getName());
        if (magicWorld == null) return;

        LivingEntity entity = event.getEntity();
        Plugin plugin = controller.getPlugin();
        spawning = true;
        try {
            LivingEntity replace =  magicWorld.processEntitySpawn(plugin, entity);
            if (replace != null && replace != entity) {
                entity.remove();
                event.setCancelled(true);
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.SEVERE, "Error replacing mob", ex);
        }
        spawning = false;
    }
}
