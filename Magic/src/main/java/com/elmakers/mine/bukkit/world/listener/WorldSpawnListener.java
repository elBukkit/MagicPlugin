package com.elmakers.mine.bukkit.world.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.magic.listener.ChunkLoadListener;
import com.elmakers.mine.bukkit.tasks.CheckChunkTask;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.WorldController;

public class WorldSpawnListener implements Listener, ChunkLoadListener
{
    private final WorldController controller;
    private Set<SpawnReason> ignoreReasons = new HashSet<>();
    private int processedSpawns = 0;
    private int processedChunkSpawns = 0;

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

    @Override
    public void onChunkLoad(Chunk chunk) {
        MagicWorld magicWorld = controller.getWorld(chunk.getWorld().getName());
        if (magicWorld == null) return;

        Plugin plugin = controller.getPlugin();
        controller.setDisableSpawnReplacement(true);
        for (Entity testEntity : chunk.getEntities()) {
            if (!(testEntity instanceof LivingEntity)) continue;
            LivingEntity entity = (LivingEntity)testEntity;
            try {
                if (magicWorld.processEntitySpawn(plugin, entity)) {
                    processedChunkSpawns++;
                    entity.remove();
                }
            } catch (Exception ex) {
                controller.getLogger().log(Level.SEVERE, "Error replacing mob", ex);
            }
        }
        controller.setDisableSpawnReplacement(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (controller.isDisableSpawnReplacement() || ignoreReasons.contains(SpawnReason.CHUNK_GEN) || !event.isNewChunk()) return;
        Chunk chunk = event.getChunk();
        if (!controller.isDataLoaded()) {
            CheckChunkTask.defer(controller.getPlugin(), this, chunk);
        } else {
            onChunkLoad(chunk);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (controller.isDisableSpawnReplacement() || ignoreReasons.contains(event.getSpawnReason())) {
            return;
        }

        MagicWorld magicWorld = controller.getWorld(event.getLocation().getWorld().getName());
        if (magicWorld == null) return;

        LivingEntity entity = event.getEntity();
        Plugin plugin = controller.getPlugin();
        controller.setDisableSpawnReplacement(true);
        try {
            if (magicWorld.processEntitySpawn(plugin, entity)) {
                entity.remove();
                event.setCancelled(true);
                processedSpawns++;
            }
        } catch (Exception ex) {
            controller.getLogger().log(Level.SEVERE, "Error replacing mob", ex);
        }
        controller.setDisableSpawnReplacement(false);
    }

    public int getProcessedSpawns() {
        return processedSpawns;
    }

    public int getProcessedChunkSpawns() {
        return processedChunkSpawns;
    }
}
