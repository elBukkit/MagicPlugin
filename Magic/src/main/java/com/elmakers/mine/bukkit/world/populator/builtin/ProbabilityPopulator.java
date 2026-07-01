package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.SplittableRandom;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.random.DistanceWeighted;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.OptionalPopulator;

public class ProbabilityPopulator extends OptionalPopulator {
    private BaseBlockPopulator populator;
    private DistanceWeighted probability;

    public boolean onLoad(ConfigurationSection config) {
        probability = DistanceWeighted.fromConfig(getLogger(), config);
        populator = BaseBlockPopulator.parsePopulator(world, config);
        return populator != null;
    }

    @Override
    protected BaseBlockPopulator getPopulator(long worldSeed, int chunkX, int chunkZ) {
        return isValid(worldSeed, chunkX, chunkZ) ? populator : null;
    }

    protected boolean isValid(long worldSeed, int chunkX, int chunkZ) {
        SplittableRandom random = RandomUtils.getStableRandom(worldSeed, chunkX, chunkZ);
        return random.nextDouble() < probability.getWeight(chunkX, chunkZ);
    }
}
