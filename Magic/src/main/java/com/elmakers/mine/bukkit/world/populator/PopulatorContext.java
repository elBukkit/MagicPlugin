package com.elmakers.mine.bukkit.world.populator;

import java.util.Random;

import org.bukkit.Material;
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

    public void setBlock(int x, int y, int z, @NotNull Material material) {
        region.setType(x, y + yOffset, z, material);
    }

    public void setBlock(int x, int y, int z, @NotNull BlockData blockData) {
        region.setBlockData(x, y + yOffset, z, blockData);
    }

    // TODO: Remove these, probably
    public WorldInfo getWorldInfo() {
        return worldInfo;
    }

    public LimitedRegion getRegion() {
        return region;
    }
}
