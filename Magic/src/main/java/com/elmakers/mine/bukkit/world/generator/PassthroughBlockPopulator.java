package com.elmakers.mine.bukkit.world.generator;

import java.util.Random;

import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

public class PassthroughBlockPopulator extends BlockPopulator {
    private final BaseChunkGenerator generator;

    public PassthroughBlockPopulator(BaseChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        generator.populate(worldInfo, random, chunkX, chunkZ, region);
    }
}
