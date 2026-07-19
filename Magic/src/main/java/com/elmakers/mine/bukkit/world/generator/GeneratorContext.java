package com.elmakers.mine.bukkit.world.generator;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.utility.IntVector;
import com.elmakers.mine.bukkit.world.WorldInfoContext;

public class GeneratorContext extends WorldInfoContext {
    private final @NotNull ChunkData chunk;

    public GeneratorContext(
        @NotNull WorldInfo worldInfo,
        @NotNull Random random,
        int chunkX,
        int chunkZ,
        @NotNull ChunkData chunk
    ) {
        this(worldInfo, random, chunkX, chunkZ, chunk, IntVector.ZERO);
    }

    protected GeneratorContext(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX,
            int chunkZ,
            @NotNull ChunkData chunk,
            IntVector offset
    ) {
        super(worldInfo, random, chunkX, chunkZ, offset);
        this.chunk = chunk;
    }

    protected GeneratorContext(GeneratorContext context, int yOffset) {
        this(context.worldInfo, context.random, context.chunkX, context.chunkZ, context.chunk, context.offset.addY(yOffset));
    }

    public GeneratorContext update(int yOffset) {
        if (yOffset == 0) {
            return this;
        }
        return new GeneratorContext(this, yOffset);
    }

    public void setBlock(int x, int y, int z, @NotNull Material material) {
        // Generators don't support x/z offsets
        y += offset.y();
        if (y < chunk.getMinHeight() || y >= chunk.getMaxHeight()) return;
        chunk.setBlock(x, y, z, material);
    }

    public void setBlock(int x, int y, int z, @NotNull BlockData blockData) {
        // Generators don't support x/z offsets
        y += offset.y();
        if (y < chunk.getMinHeight() || y >= chunk.getMaxHeight()) return;
        chunk.setBlock(x, y, z, blockData);
    }
}
