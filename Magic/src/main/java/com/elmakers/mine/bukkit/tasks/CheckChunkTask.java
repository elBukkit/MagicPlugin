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
        if (chunk.isLoaded()) {
            call(listener, chunk);
        }
    }

    protected static void call(ChunkLoadListener listener, Chunk chunk) {
        call(listener, chunk, true);
    }

    protected static void call(ChunkLoadListener listener, Chunk chunk, boolean checkEntities) {
        listener.onChunkLoad(chunk);
        if (checkEntities) {
            List<Entity> entityList = getEntityList(chunk);
            if (entityList != null) {
                listener.onEntitiesLoaded(chunk, entityList);
            }
        }
    }

    public static List<Entity> getEntityList(Chunk chunk) {
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
            // TODO: What happens with entities here in 1.17?
            defer(controller.getPlugin(), listener, chunk, 5);
        } else {
            if (CompatibilityLib.hasDeferredEntityLoad()) {
                // If we don't have the entities loaded event we need to wait 2 seconds to check this chunk
                if (!CompatibilityLib.hasEntityLoadEvent()) {
                    defer(controller.getPlugin(), listener, chunk, 20 * 2);
                } else {
                    // We'll skip looking for entities here since we need to wait for the entities load
                    // event, but we *will* trigger the chunk check right away
                    call(listener, chunk, false);
                }
            } else {
                // If this is pre-1.17 the entities will be in the chunk already, so load them now
                call(listener, chunk);
            }
        }
    }

    private static void defer(Plugin plugin, ChunkLoadListener listener, Chunk chunk, int delay) {
        plugin.getServer().getScheduler().runTaskLater(plugin, new CheckChunkTask(listener, chunk), delay);
    }
}
