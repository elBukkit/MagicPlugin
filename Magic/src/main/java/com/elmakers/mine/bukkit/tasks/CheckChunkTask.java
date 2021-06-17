package com.elmakers.mine.bukkit.tasks;

import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;

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

    public static void defer(Plugin plugin, ChunkLoadListener listener, Chunk chunk) {
        // MagicController waits 2 ticks, so we'll wait a bit longer.
        // This is kind of a hacky relationship, but I thought it preferable to retrying
        plugin.getServer().getScheduler().runTaskLater(plugin, new CheckChunkTask(listener, chunk), 5);
    }
}
