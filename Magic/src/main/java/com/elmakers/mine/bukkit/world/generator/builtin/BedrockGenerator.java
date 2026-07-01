package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class BedrockGenerator extends BaseChunkGenerator {
    @Override
    public boolean onLoad(ConfigurationSection config) {
        return true;
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        final int bedrockLevel = world.getBedrockLevel();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.setBlock(x, bedrockLevel, z, Material.BEDROCK);
            }
        }
    }
}
