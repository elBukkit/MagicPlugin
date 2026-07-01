package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.PerlinNoiseGenerator;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.DoubleRange;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class TowerPopulator extends BaseBlockPopulator {
    private PerlinNoiseGenerator perlin;
    private List<MaterialAndData> wallBlocks = Collections.emptyList();
    private IntegerRange height;
    private IntegerRange width;
    private DoubleRange taper;
    private DoubleRange taperStart;
    private DoubleRange noise;
    private DoubleRange solidAmount;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        wallBlocks = parseBlocks(config, "blocks", "stones");
        height = IntegerRange.fromConfig(getLogger(), config, "height", 32, 128);
        width = IntegerRange.fromConfig(getLogger(), config, "width", 24, 48);
        taper = DoubleRange.fromConfig(getLogger(), config, "taper", 0, 0);
        taperStart = DoubleRange.fromConfig(getLogger(), config, "taper_start", 0, 1);
        noise = DoubleRange.fromConfig(getLogger(), config, "noise", 0, 0);
        solidAmount = DoubleRange.fromConfig(getLogger(), config, "solid_amount", 0.25, 0.5);
        return true;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int worldHeight = worldInfo.getMaxHeight();
        final int towerHeight = Math.min(worldHeight - 1, height.getRandom(random));
        final double taper = this.taper.getRandom(random);
        final double noise = this.noise.getRandom(random);
        final double solidAmount = this.solidAmount.getRandom(random);
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
        final int towerWidth = Math.min(maxWidth, this.width.getRandom(random));
        final MaterialAndData wallBlock = RandomUtils.getRandom(wallBlocks, random);
        final BlockData wallBlockData = wallBlock.createBlockData();
        final int minY = floorLevel + 1;
        final int maxY = minY + towerHeight;
        final double taperStartFactor = this.taperStart.getRandom(random);
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
