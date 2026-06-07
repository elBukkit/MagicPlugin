package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.utility.random.ValueParser;
import com.elmakers.mine.bukkit.utility.random.WeightedPair;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.MagicChunkGenerator;

public class RandomGenerator extends MagicChunkGenerator {
    private Deque<WeightedPair<MagicChunkGenerator>> generators = new ArrayDeque<>();

    public void load(MagicWorld world, ConfigurationSection config) {
        super.load(world, config);
        generators.clear();
        WorldController controller = world.getController().getWorlds();
        RandomUtils.populateProbabilityMap(new ValueParser<>() {
            @Override
            public MagicChunkGenerator parse(String generatorId) {
                if (generatorId.equals("none")) return null;
                return controller.createGenerator(world, generatorId);
            }
        }, generators, config.getConfigurationSection("generators"));
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        MagicChunkGenerator generator = RandomUtils.weightedRandom(random, generators);
        if (generator == null) return;
        generator.generateSurface(worldInfo, random, chunkX, chunkZ, chunk);
    }
}
