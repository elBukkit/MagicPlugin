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
        @NotNull ChunkData chunk
    ) {
        this(worldInfo, random, chunkX, chunkZ, chunk, 0);
    }

    protected GeneratorContext(
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

    protected GeneratorContext(GeneratorContext context, int yOffset) {
        this(context.worldInfo, context.random, context.chunkX, context.chunkZ, context.chunk, context.yOffset + yOffset);
    }

    public static GeneratorContext update(GeneratorContext context, int yOffset) {
        if (yOffset == 0) {
            return context;
        }
        return new GeneratorContext(context, yOffset);
    }

    public void setBlock(int x, int y, int z, @NotNull Material material) {
        y += yOffset;
        if (y < chunk.getMinHeight() || y >= chunk.getMaxHeight()) return;
        chunk.setBlock(x, y, z, material);
    }

    public void setBlock(int x, int y, int z, @NotNull BlockData blockData) {
        y += yOffset;
        if (y < chunk.getMinHeight() || y >= chunk.getMaxHeight()) return;
        chunk.setBlock(x, y, z, blockData);
    }
}
