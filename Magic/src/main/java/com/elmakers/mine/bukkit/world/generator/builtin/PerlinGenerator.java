package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.PerlinNoiseGenerator;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class PerlinGenerator extends BaseChunkGenerator {
    private PerlinNoiseGenerator noise;
    private double noiseScale = 0.1;
    private int minElevation = 0;
    private int maxElevation = 0;
    private int maxClipElevation = 0;
    private int minClipElevation = 0;

    private List<MaterialAndData> groundBlocks = Collections.emptyList();
    private List<MaterialAndData> topBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        noiseScale = config.getDouble("noise", noiseScale);
        minElevation = config.getInt("min_elevation", minElevation);
        maxElevation = config.getInt("max_elevation", maxElevation);
        minClipElevation = config.getInt("min_clip_elevation", minClipElevation);
        maxClipElevation = config.getInt("max_clip_elevation", maxClipElevation);
        groundBlocks = parseBlocks(config, "blocks", "dirts");
        topBlocks = parseBlocks(config, "top_blocks");
        return true;
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
        final int chunkBaseX = (chunkX << 4);
        final int chunkBaseZ = (chunkZ << 4);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int worldX = chunkBaseX + x;
                final int worldZ = chunkBaseZ + z;
                final double blockValue = (noise.noise(worldX * noiseScale, worldZ * noiseScale) + 1) / 2;
                int elevation = maxElevation > 0 ? RandomUtils.lerp(minElevation, maxElevation, blockValue) : 0;
                if (maxClipElevation > 0) {
                    elevation = Math.min(elevation, maxClipElevation);
                }
                if (minClipElevation > 0) {
                    elevation = Math.max(0, elevation - minClipElevation);
                }
                final int blockIndex = RandomUtils.lerp(0, groundBlocks.size() - 1, blockValue);
                final MaterialAndData floorBlock = groundBlocks.get(blockIndex);
                MaterialAndData topBlock = floorBlock;
                if (!topBlocks.isEmpty()) {
                    final int topIndex = RandomUtils.lerp(0, topBlocks.size() - 1, blockValue);
                    topBlock = topBlocks.get(topIndex);
                }
                for (int y = bedrockLevel + 1; y <= groundLevel + elevation; y++) {
                    BlockData blockData = (y == groundLevel + elevation) ? topBlock.createBlockData() : floorBlock.createBlockData();
                    chunk.setBlock(x, y, z, blockData);
                }
                chunk.setBlock(x, bedrockLevel, z, Material.BEDROCK);
            }
        }
    }
}
