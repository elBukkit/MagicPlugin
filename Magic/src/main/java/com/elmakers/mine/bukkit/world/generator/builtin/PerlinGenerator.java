package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.PerlinNoiseGenerator;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.generator.MagicChunkGenerator;

public class PerlinGenerator extends MagicChunkGenerator {
    private PerlinNoiseGenerator noise;
    private double noiseScale = 0.1;
    private int maxElevation = 0;

    private List<MaterialAndData> groundBlocks = Collections.emptyList();

    public void load(MagicWorld world, ConfigurationSection config) {
        super.load(world, config);
        final MageController controller = world.getController();
        noiseScale = config.getDouble("noise", noiseScale);
        maxElevation = config.getInt("elevation", maxElevation);
        MaterialSet groundSet = controller.getMaterialSetManager().fromConfig(config, "blocks");
        if (groundSet == null) {
            world.getLogger().warning("Invalid block set: " + config.getString("blocks") + ", defaulting to dirts");
            groundSet = controller.getMaterialSetManager().getMaterialSet("dirts");
        }
        if (groundSet != null) {
            groundBlocks = new ArrayList<>(groundSet.getMaterialsWithData());
        }
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        synchronized (this) {
            if (noise == null) {
                noise = new PerlinNoiseGenerator(worldInfo.getSeed());
            }
        }
        final int groundLevel = world.getGroundLevel();
        final int bedrockLevel = world.getBedrockLevel();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = (chunkX << 4) + x;
                int worldZ = (chunkZ << 4) + z;
                final double blockValue = noise.noise(worldX * noiseScale, worldZ * noiseScale);
                final int elevation = maxElevation > 0 ? (int)Math.min(maxElevation, Math.max(0, (blockValue + 1) / 2 * (maxElevation + 1))) : 0;
                final int blockIndex = (int)Math.min(groundBlocks.size() - 1, Math.max(0, (blockValue + 1) / 2 * groundBlocks.size()));
                final MaterialAndData floorBlock = groundBlocks.get(blockIndex);
                for (int y = bedrockLevel + 1; y <= groundLevel + elevation; y++) {
                    chunk.setBlock(x, y, z, floorBlock.createBlockData());
                }
                chunk.setBlock(x, bedrockLevel, z, Material.BEDROCK);
            }
        }
    }
}
