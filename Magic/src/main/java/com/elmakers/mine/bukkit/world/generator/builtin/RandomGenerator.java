package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.utility.random.DistanceWeighted;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class RandomGenerator extends BaseChunkGenerator {
    private List<DistanceWeighted<BaseChunkGenerator>> generators = new ArrayList<>();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        generators.clear();
        WorldController controller = world.getController().getWorlds();
        ConfigurationSection generatorsConfig = config.getConfigurationSection("generators");
        if (generatorsConfig != null) {
            for (String generatorId : generatorsConfig.getKeys(false)) {
                if (generatorsConfig.isConfigurationSection(generatorId)) {
                    ConfigurationSection generatorConfig = generatorsConfig.getConfigurationSection(generatorId);
                    generatorId = generatorConfig.getString("generator", generatorId);
                    BaseChunkGenerator generator = controller.createGenerator(world, generatorId);
                    DistanceWeighted<BaseChunkGenerator> entry = DistanceWeighted.fromConfig(generator, generatorConfig);
                    generators.add(entry);
                } else {
                    BaseChunkGenerator generator = controller.createGenerator(world, generatorId);
                    DistanceWeighted<BaseChunkGenerator> entry = DistanceWeighted.fromString(world.getLogger(), generator, generatorsConfig.getString(generatorId));
                    generators.add(entry);
                }
            }
        } else {
            List<String> generatorIds = config.getStringList("generators");
            if (generatorIds != null) {
                for (String generatorId : generatorIds) {
                    BaseChunkGenerator generator = controller.createGenerator(world, generatorId);
                    DistanceWeighted<BaseChunkGenerator> entry = DistanceWeighted.fromString(world.getLogger(), generator, "1");
                    generators.add(entry);
                }
            } else {
                world.getController().getLogger().warning("Random generator missing 'generators' section");
            }
        }
        return !generators.isEmpty();
    }

    protected BaseChunkGenerator getGenerator(WorldInfo worldInfo, int chunkX, int chunkZ) {
        return RandomUtils.getDistanceWeighted(generators, worldInfo, chunkX, chunkZ);
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        BaseChunkGenerator generator = getGenerator(worldInfo, chunkX, chunkZ);
        if (generator != null) {
            generator.generateSurface(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    @Override
    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        BaseChunkGenerator generator = getGenerator(worldInfo, chunkX, chunkZ);
        if (generator != null) {
            generator.generateCaves(worldInfo, random, chunkX, chunkZ, chunk);
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

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo, chunkX, chunkZ);
        return generator != null && generator.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo, chunkX, chunkZ);
        return generator != null && generator.shouldGenerateSurface(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo, chunkX, chunkZ);
        return generator != null && generator.shouldGenerateCaves(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo, chunkX, chunkZ);
        return generator != null && generator.shouldGenerateDecorations(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo, chunkX, chunkZ);
        return generator != null && generator.shouldGenerateMobs(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo, chunkX, chunkZ);
        return generator != null && generator.shouldGenerateStructures(worldInfo, random, chunkX, chunkZ);
    }
}
