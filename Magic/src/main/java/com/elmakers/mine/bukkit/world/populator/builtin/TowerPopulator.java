package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.noise.PerlinNoiseGenerator;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.random.DoubleRange;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class TowerPopulator extends BaseBlockPopulator {
    private PerlinNoiseGenerator perlin;
    private List<MaterialAndData> wallBlocks = Collections.emptyList();
    private IntegerRange position;
    private IntegerRange height;
    private IntegerRange size;
    private DoubleRange taper;
    private DoubleRange taperStart;
    private DoubleRange noise;
    private DoubleRange solidAmount;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        wallBlocks = parseBlocks(config, "blocks", "stones");
        height = IntegerRange.fromConfig(getLogger(), config, "height", 32, 128);
        size = IntegerRange.fromConfig(getLogger(), config, "size", 24, 48);
        taper = DoubleRange.fromConfig(getLogger(), config, "taper", 0, 0);
        taperStart = DoubleRange.fromConfig(getLogger(), config, "taper_start", 0, 1);
        noise = DoubleRange.fromConfig(getLogger(), config, "noise", 0, 0);
        solidAmount = DoubleRange.fromConfig(getLogger(), config, "solid_amount", 0.25, 0.5);
        position = IntegerRange.fromConfig(getLogger(), config, "position", 8, 8);
        return true;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final int worldHeight = context.getMaxHeight();
        final int towerHeight = Math.min(worldHeight - 1, height.getRandom(random));
        final double taper = this.taper.getRandom(random);
        final double noise = this.noise.getRandom(random);
        final double solidAmount = this.solidAmount.getRandom(random);
        synchronized (this) {
            if (perlin == null) {
                perlin = new PerlinNoiseGenerator(context.getSeed());
            }
        }
        final int floorLevel = world.getGroundLevel();
        final BoundingBox bounds = context.getBounds();
        final int maxWidthX = bounds.size().getBlockZ();
        final int maxWidthZ = bounds.size().getBlockZ();
        final int randomSize = this.size.getRandom(random);
        final int towerWidthX = Math.min(maxWidthX, randomSize);
        final int towerWidthZ = Math.min(maxWidthZ, randomSize);
        final MaterialAndData wallBlock = RandomUtils.getRandom(wallBlocks, random);
        final BlockData wallBlockData = wallBlock.createBlockData();
        final int positionX = position.getRandom(random);
        final int positionZ = position.getRandom(random);
        final int minY = floorLevel + 1;
        final int maxY = minY + towerHeight;
        final double taperStartFactor = this.taperStart.getRandom(random);
        final int taperStart = (int)(taperStartFactor * towerHeight);
        for (int y = minY; y < maxY; y++) {
            final double taperFactor = y < taperStart ? 0 : RandomUtils.lerp(0, taper, (double)(y - taperStart) / (maxY - taperStart));
            final double taperedWidthX = (towerWidthX - taperFactor * towerWidthX) / 2;
            final double taperedWidthZ = (towerWidthZ - taperFactor * towerWidthZ) / 2;
            final int towerWidthLeftX = (int)Math.floor(taperedWidthX);
            final int towerWidthRightX = (int)Math.ceil(taperedWidthX);
            final int towerWidthLeftZ = (int)Math.floor(taperedWidthZ);
            final int towerWidthRightZ = (int)Math.ceil(taperedWidthZ);
            final int minX = positionX - towerWidthLeftX;
            final int maxX = positionX + towerWidthRightX;
            final int minZ = positionZ - towerWidthLeftZ;
            final int maxZ =  positionZ + towerWidthRightZ;
            final int midX = (minX + maxX) / 2;
            final int midZ = (minZ + maxZ) / 2;
            final int sizeXHalf = Math.abs(maxX - minX) / 2;
            final int sizeZHalf = Math.abs(maxZ - minZ) / 2;
            final int solidX = (int)(solidAmount * sizeXHalf);
            final int solidZ = (int)(solidAmount * sizeZHalf);
            for (int x = minX; x < maxX; x++) {
                final double noiseXValue = (perlin.noise(x * noise, y * noise) + 1) / 2;
                final int maxDZ = RandomUtils.lerp(solidX, sizeXHalf, noiseXValue);
                for (int z = minZ; z < maxZ; z++) {
                    final double noiseZValue = (perlin.noise(z * noise, y * noise) + 1) / 2;
                    final int maxDX = RandomUtils.lerp(solidZ, sizeZHalf, noiseZValue);
                    if (x < midX + maxDX
                        && x > midX - maxDX
                        && z < midZ + maxDZ
                        && z > midZ - maxDZ
                    ) {
                        context.setBlockData(x, y, z, wallBlockData);
                    }
                }
            }
        }
    }
}
