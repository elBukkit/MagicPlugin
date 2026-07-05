package com.elmakers.mine.bukkit.world.generator;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.world.WorldInfoContext;

public class GeneratorContext extends WorldInfoContext {
    private final @NotNull ChunkData chunk;
    private final int yOffset;

    public GeneratorContext(
        @NotNull WorldInfo worldInfo,
        @NotNull Random random,
        int chunkX,
        int chunkZ,
        @NotNull ChunkData chunk,
        int yOffset
    ) {
        super(worldInfo, random, chunkX, chunkZ);
        this.yOffset = yOffset;
        this.chunk = chunk;
    }

    protected int getTopBlock(int x, int y, int z) {
        Material material = chunk.getType(x, y, z);
        while (y < chunk.getMaxHeight() && !material.isAir()) {
            y++;
            material = chunk.getType(x, y, z);
        }
        return y - 1;
    }

    public void setBlock(int x, int y, int z, @NotNull Material material) {
        chunk.setBlock(x, y + yOffset, z, material);
    }

    public void setBlock(int x, int y, int z, @NotNull BlockData blockData) {
        chunk.setBlock(x, y + yOffset, z, blockData);
    }
}
