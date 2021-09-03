package com.elmakers.mine.bukkit.magic.listener;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;

public interface ChunkLoadListener {
    void onEntitiesLoaded(Chunk chunk, List<Entity> entities);

    default void onChunkLoad(Chunk chunk) { }
}
