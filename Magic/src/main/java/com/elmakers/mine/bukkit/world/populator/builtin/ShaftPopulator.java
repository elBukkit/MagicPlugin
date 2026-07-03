package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class ShaftPopulator extends BaseBlockPopulator {
    private IntegerRange shaftWidth;
    private IntegerRange shaftHeight;
    private IntegerRange position;
    private IntegerRange rimWidth;
    private IntegerRange yOffset;
    private double floorProbability;
    private List<MaterialAndData> wallBlocks = Collections.emptyList();
    private List<MaterialAndData> fillBlocks = Collections.emptyList();
    private List<MaterialAndData> rimBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        shaftWidth = IntegerRange.fromConfig(getLogger(), config, "width", 3, 3);
        shaftHeight = IntegerRange.fromConfig(getLogger(), config, "height", 2, 4);
        position = IntegerRange.fromConfig(getLogger(), config, "position", 8, 8);
        rimWidth = IntegerRange.fromConfig(getLogger(), config, "rim_width", 0, 0);
        yOffset = IntegerRange.fromConfig(getLogger(), config, "y_offset", 0, 0);
        floorProbability = config.getDouble("floor_probability", 0);
        wallBlocks = parseBlocks(config, "blocks", "stones");
        fillBlocks = parseBlocks(config, "fill", "air");
        rimBlocks = parseBlocks(config, "rim", wallBlocks);
        return shaftWidth.getMax() > 0;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int centerX = position.getRandom(random);
        final int centerZ = position.getRandom(random);
        final int shaftWidth = this.shaftWidth.getRandom(random);
        final int shaftHeight = this.shaftHeight.getRandom(random);
        final int rimSize = this.rimWidth.getRandom(random);
        final int yOffset = this.yOffset.getRandom(random);
        final boolean isFloor = random.nextDouble() < floorProbability;
        final BlockData wallBlock = RandomUtils.getRandom(wallBlocks, random).createBlockData();
        final BlockData fillBlock = RandomUtils.getRandom(fillBlocks, random).createBlockData();
        final int startY = getSolidFloorOrCeiling(isFloor, worldInfo, region, chunkGlobalX + centerX, world.getBedrockLevel(), chunkGlobalZ + centerZ) + yOffset;
        final int direction = isFloor ? -1 : 1;
        final int stopY = isFloor ? Math.max(worldInfo.getMinHeight() + 1, startY - shaftHeight) : Math.min(worldInfo.getMaxHeight() - 1, startY + shaftHeight);
        // This includes the border, so it is width + 1 on both sizes
        final int start = centerX - (shaftWidth / 2) - 1;
        final int end = start + shaftWidth + 1;

        for (int dx = start; dx <= end; dx++) {
            for (int dz = start; dz <= end; dz++) {
                final int x = chunkGlobalX + dx;
                final int z = chunkGlobalZ + dz;
                for (int y = startY; y != stopY; y += direction) {
                    if (dx == start || dz == start || dx == end || dz == end) {
                        region.setBlockData(x, y, z, wallBlock);
                    } else {
                        region.setBlockData(x, y, z, fillBlock);
                    }
                }
            }
        }

        // Build rims
        if (rimSize > 0) {
            final BlockData rimBlock = RandomUtils.getRandom(rimBlocks, random).createBlockData();
            for (int dx = start - rimSize; dx <= end + rimSize; dx++) {
                for (int dz = start - rimSize; dz <= end + rimSize; dz++) {
                    if (dx < start || dx > end || dz < start || dz > end) {
                        final int x = chunkGlobalX + dx;
                        final int z = chunkGlobalZ + dz;
                        region.setBlockData(x, startY, z, rimBlock);
                        region.setBlockData(x, stopY - direction, z, rimBlock);
                    }
                }
            }
        }
    }
}
