package com.elmakers.mine.bukkit.world.generator.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.world.WorldContext;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;
import com.elmakers.mine.bukkit.world.generator.GeneratorContext;

public class VanillaGenerator extends BaseChunkGenerator {
    private boolean generateNoise = true;
    private boolean generateSurface = true;
    private boolean generateCaves = true;
    private boolean generateDecorations = true;
    private boolean generateMobs = true;
    private boolean generateStructures = true;

    @Override
    public boolean onLoad(ConfigurationSection configuration) {
        generateNoise = configuration.getBoolean("generate_noise", generateNoise);
        generateSurface = configuration.getBoolean("generate_surface", generateSurface);
        generateCaves = configuration.getBoolean("generate_caves", generateCaves);
        generateDecorations = configuration.getBoolean("generate_decorations", generateDecorations);
        generateMobs = configuration.getBoolean("generate_mobs", generateMobs);
        generateStructures = configuration.getBoolean("generate_structures", generateStructures);
        return true;
    }

    @Override
    public void generate(GeneratorContext context) {

    }

    @Override
    public boolean shouldGenerateNoise(@NotNull WorldContext context) {
        return generateNoise;
    }

    @Override
    public boolean shouldGenerateSurface(@NotNull WorldContext context) {
        return generateSurface;
    }

    @Override
    public boolean shouldGenerateCaves(@NotNull WorldContext context) {
        return generateCaves;
    }

    @Override
    public boolean shouldGenerateDecorations(@NotNull WorldContext context) {
        return generateDecorations;
    }

    @Override
    public boolean shouldGenerateMobs(@NotNull WorldContext context) {
        return generateMobs;
    }

    @Override
    public boolean shouldGenerateStructures(@NotNull WorldContext context) {
        return generateStructures;
    }
}
