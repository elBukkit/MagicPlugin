package com.elmakers.mine.bukkit.world.populator;

import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.world.WorldInfoContext;

public class PopulatorContext extends WorldInfoContext {
    private final @NotNull LimitedRegion region;
    private final int yOffset;

    public PopulatorContext(
        @NotNull WorldInfo worldInfo,
        @NotNull Random random,
        int chunkX,
        int chunkZ,
        @NotNull LimitedRegion region,
        int yOffset
    ) {
        super(worldInfo, random, chunkX, chunkZ);
        this.region = region;
        this.yOffset = yOffset;
    }

    public int getWorldY(int y) {
        return y + yOffset;
    }

    public void setBlock(int x, int y, int z, @NotNull Material material) {
        region.setType(getWorldX(x), getWorldY(y), getWorldZ(z), material);
    }

    public void setBlock(int x, int y, int z, @NotNull BlockData blockData) {
        region.setBlockData(getWorldX(x), getWorldY(y), getWorldZ(z), blockData);
    }

    public BlockData getBlockData(int x, int y, int z) {
        return region.getBlockData(getWorldX(x), getWorldY(y), getWorldZ(z));
    }

    public BlockState getBlockState(int x, int y, int z) {
        return region.getBlockState(getWorldX(x), getWorldY(y), getWorldZ(z));
    }

    public Biome getBiome(int x, int y, int z) {
        return region.getBiome(getWorldX(x), getWorldY(y), getWorldZ(z));
    }

    public List<BlockState> getTileEntities() {
        return region.getTileEntities();
    }

    public boolean isCenterChunk(Chunk chunk) {
        return chunk.getX() == chunkX && chunk.getZ() == chunkZ;
    }

    public PopulatorContext addYOffset(int yOffset) {
        if (yOffset == 0) return this;
        return new PopulatorContext(worldInfo, random, chunkX, chunkZ, region, this.yOffset + yOffset);
    }

    // TODO: Remove these, probably
    public WorldInfo getWorldInfo() {
        return worldInfo;
    }

    public LimitedRegion getRegion() {
        return region;
    }
}
