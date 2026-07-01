package com.elmakers.mine.bukkit.world.generator;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

public abstract class OptionalGenerator extends BaseChunkGenerator {
    protected abstract BaseChunkGenerator getGenerator(long worldSeed, int chunkX, int chunkZ);

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        BaseChunkGenerator generator = getGenerator(worldInfo.getSeed(), chunkX, chunkZ);
        if (generator != null) {
            generator.generateSurface(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    @Override
    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        BaseChunkGenerator generator = getGenerator(worldInfo.getSeed(), chunkX, chunkZ);
        if (generator != null) {
            generator.generateCaves(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        super.populate(worldInfo, random, chunkX, chunkZ, region);
        BaseChunkGenerator generator = getGenerator(worldInfo.getSeed(), chunkX, chunkZ);
        if (generator != null) {
            generator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo.getSeed(), chunkX, chunkZ);
        return generator != null && generator.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo.getSeed(), chunkX, chunkZ);
        return generator != null && generator.shouldGenerateSurface(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo.getSeed(), chunkX, chunkZ);
        return generator != null && generator.shouldGenerateCaves(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo.getSeed(), chunkX, chunkZ);
        return generator != null && generator.shouldGenerateDecorations(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo.getSeed(), chunkX, chunkZ);
        return generator != null && generator.shouldGenerateMobs(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        BaseChunkGenerator generator = getGenerator(worldInfo.getSeed(), chunkX, chunkZ);
        return generator != null && generator.shouldGenerateStructures(worldInfo, random, chunkX, chunkZ);
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        String baseTargetWorld = super.getPortalTargetWorld(location);
        if (baseTargetWorld != null) {
            return baseTargetWorld;
        }
        BaseChunkGenerator generator = getGenerator(location.getWorld().getSeed(), location.getChunk().getX(), location.getChunk().getZ());
        if (generator == null) {
            return null;
        }
        return generator.getPortalTargetWorld(location);
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        BiomeProvider biomeProvider = super.getDefaultBiomeProvider(worldInfo);
        if (biomeProvider != null) {
            return biomeProvider;
        }
        BaseChunkGenerator generator = getGenerator(worldInfo.getSeed(), 0, 0);
        return generator != null ? generator.getDefaultBiomeProvider(worldInfo) : null;
    }
}
