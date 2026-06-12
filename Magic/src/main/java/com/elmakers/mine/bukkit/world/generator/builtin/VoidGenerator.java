package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class VoidGenerator extends BaseChunkGenerator {
    @Override
    public void onLoad(ConfigurationSection config) {
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
    }
}
