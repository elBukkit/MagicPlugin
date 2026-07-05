package com.elmakers.mine.bukkit.world.generator;

import java.util.Random;

import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class PassthroughBlockPopulator extends BlockPopulator {
    private final BaseChunkGenerator generator;
    private int yOffset = 0;

    public PassthroughBlockPopulator(BaseChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final PopulatorContext context = new PopulatorContext(worldInfo, random, chunkX, chunkZ, region, yOffset);
        generator.populate(context);
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }
}
