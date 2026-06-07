package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.world.MagicWorld;
import com.elmakers.mine.bukkit.world.generator.MagicChunkGenerator;

public class PerlinGenerator extends MagicChunkGenerator {
    private PerlinNoiseGenerator noise;
    private double noiseScale = 0.1;
    private int groundLevel = 2;
    private int bedrockLevel = 1;
    private int maxElevation = 0;
    private double foodProbability = 0;

    private List<MaterialAndData> groundBlocks = Collections.emptyList();

    public void load(MagicWorld world, ConfigurationSection config) {
        super.load(world, config);
        final MageController controller = world.getController();
        groundLevel = config.getInt("ground_level", groundLevel);
        bedrockLevel = config.getInt("bedrock_level", bedrockLevel);
        foodProbability = config.getDouble("food_probability", foodProbability);
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
        final boolean hasFood = random.nextDouble() < foodProbability;
        final int groundLevel = this.groundLevel;
        final int bedrockLevel = this.bedrockLevel;
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
        return new Location(world, 0, groundLevel + 1 + maxElevation, 0);
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
