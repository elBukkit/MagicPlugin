package com.elmakers.mine.bukkit.tasks;

import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.listener.ChunkLoadListener;

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
            listener.onChunkLoad(this.chunk);
        }
    }

    public static void process(MagicController controller, ChunkLoadListener listener, Chunk chunk) {
        // This is hopefully temporary
        // Works around async entity loading by waiting a bit to look for them.
        defer(controller.getPlugin(), listener, chunk);
        /*
        if (!controller.isDataLoaded()) {
            defer(controller.getPlugin(), listener, chunk);
        } else {
            listener.onChunkLoad(chunk);
        }
        */
    }

    private static void defer(Plugin plugin, ChunkLoadListener listener, Chunk chunk) {
        // MagicController waits 2 ticks, so we'll wait a bit longer.
        // This is kind of a hacky relationship, but I thought it preferable to retrying
        // we are now waiting a full 2 seconds to work around issues in 1.17
        plugin.getServer().getScheduler().runTaskLater(plugin, new CheckChunkTask(listener, chunk), 20 * 2);
    }
}
