package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.PerlinNoiseGenerator;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.generator.MagicChunkGenerator;

public class FlatGenerator extends MagicChunkGenerator {
    private PerlinNoiseGenerator noise;
    private double noiseScale = 0.1;
    private int groundLevel = 2;
    private int bedrockLevel = 1;
    private double foodProbability = 0;

    private Material[] floorBlocks = {
            Material.BLACK_CONCRETE,
            Material.BLACK_CONCRETE_POWDER,
            Material.GRAY_CONCRETE,
            Material.GRAY_CONCRETE_POWDER,
            Material.LIGHT_GRAY_CONCRETE,
            Material.LIGHT_GRAY_CONCRETE_POWDER,
            Material.WHITE_CONCRETE,
            Material.WHITE_CONCRETE_POWDER
    };

    public void load(MagicWorld world, ConfigurationSection config) {
        super.load(world, config);
        final MageController controller = world.getController();
        groundLevel = config.getInt("ground_level", groundLevel);
        bedrockLevel = config.getInt("bedrock_level", bedrockLevel);
        foodProbability = config.getDouble("food_probability", foodProbability);
        noiseScale = config.getDouble("noise_scale", noiseScale);
        // FLOOR_BLOCKS = controller.getMaterialSetManager.get(config, "floor_blocks", FLOOR_BLOCKS);
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunk) {
        synchronized (this) {
            if (noise == null) {
                noise = new PerlinNoiseGenerator(worldInfo.getSeed());
            }
        }
        final boolean hasFood = random.nextDouble() < foodProbability;
        final int groundLevel = this.groundLevel;
        final int bedrockLevel = this.bedrockLevel;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = (chunkX << 4) + x;
                int worldZ = (chunkZ << 4) + z;

                final double blockValue = noise.noise(worldX * noiseScale, worldZ * noiseScale);
                final int blockIndex = (int)Math.min(floorBlocks.length - 1, Math.max(0, (blockValue + 1) / 2 * floorBlocks.length));
                final Material floorBlock = floorBlocks[blockIndex];
                for (int y = bedrockLevel + 1; y <= groundLevel; y++) {
                    chunk.setBlock(x, y, z, floorBlock);
                }
                chunk.setBlock(x, this.bedrockLevel, z, Material.BEDROCK);
            }
        }

        if (hasFood) {
            final BlockData blockData = getPlugin().getServer().createBlockData(Material.CARROTS);
            if (blockData instanceof Ageable) {
                Ageable crops = (Ageable)blockData;
                crops.setAge(crops.getMaximumAge());
            }
            for (int x = 6; x < 10; x++) {
                for (int z = 6; z < 10; z++) {
                    if (x == 6 || x == 9 || z == 6 || z == 9) {
                        chunk.setBlock(x, groundLevel + 1, z, Material.OAK_PRESSURE_PLATE);
                    } else {
                        chunk.setBlock(x, groundLevel + 1, z, blockData);
                    }
                }
            }
        }
    }

    @Override
    public Location getSpawnLocation(World world) {
        return new Location(world, 0, groundLevel + 1, 0);
    }

    @Override
    public int getGroundLevel() {
        return groundLevel;
    }

    @Override
    public int getBedrockLevel() {
        return bedrockLevel;
    }
}
