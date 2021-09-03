package com.elmakers.mine.bukkit.tasks;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.listener.ChunkLoadListener;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class CheckChunkTask implements Runnable {
    private final ChunkLoadListener listener;
    private final Chunk chunk;

    public CheckChunkTask(ChunkLoadListener listener, Chunk chunk) {
        this.listener = listener;
        this.chunk = chunk;
    }

    @Override
    public void run() {
        // Ignore the event if the chunk already unloaded
        if (this.chunk.isLoaded()) {
            listener.onChunkLoad(this.chunk, getEntityList(chunk));
        }
    }

    private static List<Entity> getEntityList(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        List<Entity> entityList = null;
        if (entities.length > 0) {
            entityList = Arrays.asList(entities);
        }
        return entityList;
    }

    public static void process(MagicController controller, ChunkLoadListener listener, Chunk chunk) {
        // Wait until everything is loaded before we process these chunks
        if (!controller.isDataLoaded()) {
            // MagicController waits 2 ticks, so we'll wait a bit longer.
            defer(controller.getPlugin(), listener, chunk, 5);
        } else {
            if (CompatibilityLib.hasDeferredEntityLoad()) {
                // If we don't have the entities loaded event we need to wait 2 seconds to check this chunk
                if (!CompatibilityLib.hasEntityLoadEvent()) {
                    defer(controller.getPlugin(), listener, chunk, 20 * 2);
                }
            } else {
                // If this is pre-1.17 the entities will be in the chunk already, so load them now
                listener.onChunkLoad(chunk, getEntityList(chunk));
            }
        }
    }

    private static void defer(Plugin plugin, ChunkLoadListener listener, Chunk chunk, int delay) {
        plugin.getServer().getScheduler().runTaskLater(plugin, new CheckChunkTask(listener, chunk), delay);
    }
}
