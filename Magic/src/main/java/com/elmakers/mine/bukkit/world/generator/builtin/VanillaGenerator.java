package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class VanillaGenerator extends BaseChunkGenerator {
    private boolean generateNoise = true;
    private boolean generateSurface = true;
    private boolean generateCaves = true;
    private boolean generateDecorations = true;
    private boolean generateMobs = true;
    private boolean generateStructures = true;

    @Override
    public void onLoad(ConfigurationSection configuration) {
        generateNoise = configuration.getBoolean("generate_noise", generateNoise);
        generateSurface = configuration.getBoolean("generate_surface", generateSurface);
        generateCaves = configuration.getBoolean("generate_caves", generateCaves);
        generateDecorations = configuration.getBoolean("generate_decorations", generateDecorations);
        generateMobs = configuration.getBoolean("generate_mobs", generateMobs);
        generateStructures = configuration.getBoolean("generate_structures", generateStructures);
    }

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return generateNoise;
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return generateSurface;
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return generateCaves;
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return generateDecorations;
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return generateMobs;
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
        return generateStructures;
    }
}
