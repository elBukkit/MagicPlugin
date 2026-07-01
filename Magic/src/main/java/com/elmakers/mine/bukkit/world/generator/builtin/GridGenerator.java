package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class GridGenerator extends BaseChunkGenerator {
    private BaseChunkGenerator fallbackGenerator;
    private final Map<Integer, Map<Integer, BaseChunkGenerator>> generatorMap = new HashMap<>();

    public boolean onLoad(ConfigurationSection config) {
        WorldController controller = world.getController().getWorlds();
        fallbackGenerator = config.contains("fallback") ? controller.parseGenerator(world, config, "fallback") : null;
        ConfigurationSection gridConfig = config.getConfigurationSection("generators");
        for (String entryKey : gridConfig.getKeys(false)) {
            ConfigurationSection entry = gridConfig.getConfigurationSection(entryKey);
            final int x = entry.getInt("x", 0);
            final int z = entry.getInt("z", 0);
            BaseChunkGenerator generator;
            if (entry.contains("generator") || entry.contains("class")) {
                generator = controller.parseGenerator(world, entry);
            } else {
                generator = controller.createGenerator(world, entryKey);
            }
            if (generator != null) {
                Map<Integer, BaseChunkGenerator> map = generatorMap.computeIfAbsent(x, m -> new HashMap<>());
                map.put(z, generator);
            }
        }
        return fallbackGenerator != null || !generatorMap.isEmpty();
    }

    protected BaseChunkGenerator getGenerator(int chunkX, int chunkZ) {
        Map<Integer, BaseChunkGenerator> map = generatorMap.get(chunkX);
        if (map == null) {
            return fallbackGenerator;
        }
        BaseChunkGenerator generator = map.get(chunkZ);
        return generator != null ? generator : fallbackGenerator;
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        BaseChunkGenerator generator = getGenerator(chunkX, chunkZ);
        if (generator != null) {
            generator.generateSurface(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    @Override
    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        BaseChunkGenerator generator = getGenerator(chunkX, chunkZ);
        if (generator != null) {
            generator.generateCaves(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        super.populate(worldInfo, random, chunkX, chunkZ, region);
        BaseChunkGenerator generator = getGenerator(chunkX, chunkZ);
        if (generator != null) {
            generator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(chunkX, chunkZ);
        return generator != null && generator.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(chunkX, chunkZ);
        return generator != null && generator.shouldGenerateSurface(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(chunkX, chunkZ);
        return generator != null && generator.shouldGenerateCaves(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(chunkX, chunkZ);
        return generator != null && generator.shouldGenerateDecorations(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(chunkX, chunkZ);
        return generator != null && generator.shouldGenerateMobs(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(chunkX, chunkZ);
        return generator != null && generator.shouldGenerateStructures(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        String baseTargetWorld = super.getPortalTargetWorld(location);
        if (baseTargetWorld != null) {
            return baseTargetWorld;
        }
        BaseChunkGenerator generator = getGenerator(location.getChunk().getX(), location.getChunk().getZ());
        if (generator == null) {
            return null;
        }
        return generator.getPortalTargetWorld(location);
    }
}
