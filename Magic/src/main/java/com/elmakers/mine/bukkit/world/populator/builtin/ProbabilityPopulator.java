package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;
import java.util.SplittableRandom;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.DistanceWeighted;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class ProbabilityPopulator extends BaseBlockPopulator {
    private BaseBlockPopulator populator;
    private DistanceWeighted probability;

    public boolean onLoad(ConfigurationSection config) {
        probability = DistanceWeighted.fromConfig(getLogger(), config);
        populator = BaseBlockPopulator.parsePopulator(world, config);
        return populator != null;
    }

    protected boolean isValid(long worldSeed, int chunkX, int chunkZ) {
        SplittableRandom random = RandomUtils.getStableRandom(worldSeed, chunkX, chunkZ);
        return random.nextDouble() < probability.getWeight(chunkX, chunkZ);
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        if (isValid(worldInfo.getSeed(), chunkX, chunkZ)) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        if (isValid(location.getWorld().getSeed(), location.getChunk().getX(), location.getChunk().getZ())) {
            return populator.getPortalTargetWorld(location);
        }
        return null;
    }
}
