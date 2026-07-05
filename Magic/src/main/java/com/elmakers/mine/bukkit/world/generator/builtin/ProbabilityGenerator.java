package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.SplittableRandom;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.random.DistanceWeighted;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.WorldContext;
import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.generator.OptionalGenerator;

public class ProbabilityGenerator extends OptionalGenerator {
    private BaseChunkGenerator generator;
    private DistanceWeighted probability;

    public boolean onLoad(ConfigurationSection config) {
        probability = DistanceWeighted.fromConfig(getLogger(), config);
        WorldController controller = world.getController().getWorlds();
        generator = controller.parseGenerator(world, config);
        return generator != null;
    }

    protected boolean isValid(long worldSeed, int chunkX, int chunkZ) {
        SplittableRandom random = RandomUtils.getStableRandom(worldSeed, chunkX, chunkZ);
        return random.nextDouble() < probability.getWeight(chunkX, chunkZ);
    }

    @Override
    protected BaseChunkGenerator getGenerator(WorldContext context) {
        return isValid(context.getSeed(), context.getChunkX(), context.getChunkZ()) ? generator : null;
    }
}
