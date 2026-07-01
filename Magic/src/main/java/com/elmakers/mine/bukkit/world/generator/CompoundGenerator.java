package com.elmakers.mine.bukkit.world.generator;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

public abstract class CompoundGenerator extends BaseChunkGenerator {
    protected abstract List<BaseChunkGenerator> getGenerators(long worldSeed, int chunkX, int chunkZ);

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        for (BaseChunkGenerator generator : getGenerators(worldInfo.getSeed(), chunkX, chunkZ)) {
            generator.generateSurface(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    @Override
    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        for (BaseChunkGenerator generator : getGenerators(worldInfo.getSeed(), chunkX, chunkZ)) {
            generator.generateCaves(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        super.populate(worldInfo, random, chunkX, chunkZ, region);
        for (BaseChunkGenerator generator : getGenerators(worldInfo.getSeed(), chunkX, chunkZ)) {
            generator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        for (BaseChunkGenerator generator : getGenerators(worldInfo.getSeed(), chunkX, chunkZ)) {
            if (generator.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        for (BaseChunkGenerator generator : getGenerators(worldInfo.getSeed(), chunkX, chunkZ)) {
            if (generator.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        for (BaseChunkGenerator generator : getGenerators(worldInfo.getSeed(), chunkX, chunkZ)) {
            if (generator.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        for (BaseChunkGenerator generator : getGenerators(worldInfo.getSeed(), chunkX, chunkZ)) {
            if (generator.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        for (BaseChunkGenerator generator : getGenerators(worldInfo.getSeed(), chunkX, chunkZ)) {
            if (generator.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        for (BaseChunkGenerator generator : getGenerators(worldInfo.getSeed(), chunkX, chunkZ)) {
            if (generator.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        for (BaseChunkGenerator generator : getGenerators(location.getWorld().getSeed(), location.getChunk().getX(), location.getChunk().getZ())) {
            String targetWorld = generator.getPortalTargetWorld(location);
            if (targetWorld != null) {
                return targetWorld;
            }
        }
        return null;
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        BiomeProvider biomeProvider = super.getDefaultBiomeProvider(worldInfo);
        if (biomeProvider != null) {
            return biomeProvider;
        }
        for (BaseChunkGenerator generator : getGenerators(worldInfo.getSeed(), 0, 0)) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
            if (biomeProvider != null) {
                return biomeProvider;
            }
        }
        return null;
    }
}
