package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.world.WorldController;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class FrequencyGenerator extends BaseChunkGenerator {
    private BaseChunkGenerator generator;
    private int frequency = 2;

    public boolean onLoad(ConfigurationSection config) {
        frequency = config.getInt("frequency", frequency);
        WorldController controller = world.getController().getWorlds();
        generator = controller.parseGenerator(world, config);
        return generator != null;
    }

    protected boolean isValid(int chunkX, int chunkZ) {
        return chunkX % frequency == 0 && chunkZ % frequency == 0;
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        if (isValid(chunkX, chunkZ)) {
            generator.generateSurface(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    @Override
    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        if (isValid(chunkX, chunkZ)) {
            generator.generateCaves(worldInfo, random, chunkX, chunkZ, chunk);
        }
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        if (isValid(chunkX, chunkZ)) {
            super.populate(worldInfo, random, chunkX, chunkZ, region);
            generator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        if (isValid(chunkX, chunkZ)) {
            return generator.shouldGenerateNoise(worldInfo, random, chunkX, chunkZ);
        }
        return false;
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        if (isValid(chunkX, chunkZ)) {
            return generator.shouldGenerateSurface(worldInfo, random, chunkX, chunkZ);
        }
        return false;
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        if (isValid(chunkX, chunkZ)) {
            return generator.shouldGenerateCaves(worldInfo, random, chunkX, chunkZ);
        }
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        if (isValid(chunkX, chunkZ)) {
            return generator.shouldGenerateDecorations(worldInfo, random, chunkX, chunkZ);
        }
        return false;
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        if (isValid(chunkX, chunkZ)) {
            return generator.shouldGenerateMobs(worldInfo, random, chunkX, chunkZ);
        }
        return false;
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        if (isValid(chunkX, chunkZ)) {
            return generator.shouldGenerateStructures(worldInfo, random, chunkX, chunkZ);
        }
        return false;
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        if (isValid(location.getChunk().getX(), location.getChunk().getZ())) {
            return generator.getPortalTargetWorld(location);
        }
        return null;
    }
}
