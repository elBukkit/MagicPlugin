package com.elmakers.mine.bukkit.utility.platform;

import java.util.function.Consumer;

import org.bukkit.Chunk;
import org.bukkit.World;

public interface PaperUtils {
    void loadChunk(World world, int x, int z, boolean generate, Consumer<Chunk> consumer);
}
