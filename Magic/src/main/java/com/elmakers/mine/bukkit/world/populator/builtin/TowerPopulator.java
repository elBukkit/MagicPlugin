package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.PerlinNoiseGenerator;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class TowerPopulator extends BaseBlockPopulator {
    private PerlinNoiseGenerator perlin;
    private List<MaterialAndData> wallBlocks = Collections.emptyList();
    private int minHeight = 32;
    private int maxHeight = 128;
    private int minWidth = 24;
    private int maxWidth = 48;
    private double minTaper = 0;
    private double maxTaper = 0;
    private double minTaperStart = 0;
    private double maxTaperStart = 1;
    private double minNoise = 0;
    private double maxNoise = 0;
    private double minSolidAmount = 0.25;
    private double maxSolidAmount = 0.5;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        MagicController controller = world.getController();
        MaterialSet wallSet = controller.getMaterialSetManager().fromConfig(config, "blocks");
        if (wallSet == null) {
            world.getLogger().warning("Invalid block set: " + config.getString("blocks") + ", defaulting to stones");
            wallSet = controller.getMaterialSetManager().getMaterialSet("stones");
        }
        if (wallSet != null) {
            wallBlocks = new ArrayList<>(wallSet.getMaterialsWithData());
        }
        minHeight = config.getInt("min_height", minHeight);
        maxHeight = config.getInt("max_height", maxHeight);
        minWidth = config.getInt("min_width", minWidth);
        maxWidth = config.getInt("max_width", maxWidth);
        minNoise = config.getDouble("min_noise", minNoise);
        maxNoise = config.getDouble("max_noise", maxNoise);
        minSolidAmount = config.getDouble("min_solid_amount", minSolidAmount);
        maxSolidAmount = config.getDouble("max_solid_amount", maxSolidAmount);
        minTaper = config.getDouble("min_taper", minTaper);
        maxTaper = config.getDouble("max_taper", maxTaper);
        minTaperStart = config.getDouble("min_taper_start", minTaperStart);
        maxTaperStart = config.getDouble("max_taper_start", maxTaperStart);
        return true;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int worldHeight = worldInfo.getMaxHeight();
        final int towerHeight = Math.min(worldHeight - 1, RandomUtils.range(random, minHeight, maxHeight));
        final double taper = RandomUtils.range(random, minTaper, maxTaper);
        final double noise = RandomUtils.range(random, minNoise, maxNoise);
        final double solidAmount = RandomUtils.range(random, minSolidAmount, maxSolidAmount);
        synchronized (this) {
            if (perlin == null) {
                perlin = new PerlinNoiseGenerator(worldInfo.getSeed());
            }
        }
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = world.getGroundLevel();
        final int buffer = region.getBuffer();
        final int maxWidth = 16 + buffer * 2;
        final int towerWidth = Math.min(maxWidth, RandomUtils.range(random, minWidth, maxWidth));
        final MaterialAndData wallBlock = RandomUtils.getRandom(wallBlocks, random);
        final BlockData wallBlockData = wallBlock.createBlockData();
        final int minY = floorLevel + 1;
        final int maxY = minY + towerHeight;
        final double taperStartFactor = RandomUtils.range(random, minTaperStart, maxTaperStart);
        final int taperStart = (int)(taperStartFactor * towerHeight);
        for (int y = minY; y < maxY; y++) {
            final double taperFactor = y < taperStart ? 0 : RandomUtils.lerp(0, taper, (double)(y - taperStart) / (maxY - taperStart));
            final double taperedWidth = (towerWidth - taperFactor * towerWidth) / 2;
            final int towerWidthLeft = (int)Math.floor(taperedWidth);
            final int towerWidthRight = (int)Math.ceil(taperedWidth);
            final int minX = 8 - towerWidthLeft;
            final int maxX = 8 + towerWidthRight;
            final int minZ = 8 - towerWidthLeft;
            final int maxZ =  8 + towerWidthRight;
            final int midX = (minX + maxX) / 2;
            final int midZ = (minZ + maxZ) / 2;
            final int sizeXHalf = Math.abs(maxX - minX) / 2;
            final int sizeZHalf = Math.abs(maxZ - minZ) / 2;
            final int solidX = (int)(solidAmount * sizeXHalf);
            final int solidZ = (int)(solidAmount * sizeZHalf);
            for (int dx = minX; dx < maxX; dx++) {
                final int x = chunkGlobalX + dx;
                final double noiseXValue = (perlin.noise(x * noise, y * noise) + 1) / 2;
                final int maxDZ = RandomUtils.lerp(solidX, sizeXHalf, noiseXValue);
                for (int dz = minZ; dz < maxZ; dz++) {
                    final int z = chunkGlobalZ + dz;
                    final double noiseZValue = (perlin.noise(z * noise, y * noise) + 1) / 2;
                    final int maxDX = RandomUtils.lerp(solidZ, sizeZHalf, noiseZValue);
                    if (x < chunkGlobalX + midX + maxDX
                        && x > chunkGlobalX + midX - maxDX
                        && z < chunkGlobalZ + midZ + maxDZ
                        && z > chunkGlobalZ + midZ - maxDZ
                    ) {
                        setBlockData(region, x, y, z, wallBlockData);
                    }
                }
            }
        }
    }
}
