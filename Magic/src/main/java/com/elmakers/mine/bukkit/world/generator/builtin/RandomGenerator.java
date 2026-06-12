package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.utility.random.DistanceWeighted;
import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class RandomGenerator extends BaseChunkGenerator {
    private List<DistanceWeighted<BaseChunkGenerator>> generators = new ArrayList<>();

    @Override
    public void onLoad(ConfigurationSection config) {
        generators.clear();
        WorldController controller = world.getController().getWorlds();
        ConfigurationSection generatorsConfig = config.getConfigurationSection("generators");
        for (String generatorId : generatorsConfig.getKeys(false)) {
            if (generatorsConfig.isConfigurationSection(generatorId)) {
                ConfigurationSection generatorConfig = generatorsConfig.getConfigurationSection(generatorId);
                generatorId = generatorConfig.getString("generator", generatorId);
                BaseChunkGenerator generator = controller.createGenerator(world, generatorId);
                DistanceWeighted<BaseChunkGenerator> entry = DistanceWeighted.fromConfig(generator, generatorConfig);
                generators.add(entry);
            } else {
                BaseChunkGenerator generator = controller.createGenerator(world, generatorId);
                DistanceWeighted entry = DistanceWeighted.fromString(world.getLogger(), generator, generatorsConfig.getString(generatorId));
                generators.add(entry);
            }
        }
    }

    protected BaseChunkGenerator getGenerator(WorldInfo worldInfo, int chunkX, int chunkZ) {
        long worldSeed = worldInfo.getSeed();
        final long chunkSeed = worldSeed
                ^ (long) chunkX * 0x9E3779B97F4A7C15L
                ^ (long) chunkZ * 0xD1B54A32D192ED03L;

        double totalWeight = 0;
        final int x = chunkX * 16;
        final int z = chunkX * 16;
        for (DistanceWeighted<BaseChunkGenerator> entry : generators) {
            totalWeight += entry.getWeight(x, z);
        }
        if (totalWeight == 0) {
            return generators.get(0).getValue();
        }

        double weight = new SplittableRandom(chunkSeed).nextDouble(totalWeight);
        for (DistanceWeighted<BaseChunkGenerator> entry : generators) {
            double entryWeight = entry.getWeight(x, z);
            if (entryWeight <= 0) {
                continue;
            }
            weight -= entryWeight;
            if (weight <= 0) {
                return entry.getValue();
            }
        }
        // Should never happen
        return generators.get(generators.size() - 1).getValue();
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        BaseChunkGenerator generator = getGenerator(worldInfo, chunkX, chunkZ);
        if (generator != null) {
            generator.generateSurface(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        super.populate(worldInfo, random, chunkX, chunkZ, region);
        BaseChunkGenerator generator = getGenerator(worldInfo, chunkX, chunkZ);
        if (generator != null) {
            generator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }
}
