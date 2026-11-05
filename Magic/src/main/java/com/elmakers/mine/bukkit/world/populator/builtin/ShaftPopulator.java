package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class ShaftPopulator extends BaseBlockPopulator {
    private IntegerRange shaftSize;
    private IntegerRange shaftHeight;
    private IntegerRange position;
    private IntegerRange rimSize;
    private IntegerRange yOffset;
    private double floorProbability;
    private List<MaterialAndData> wallBlocks = Collections.emptyList();
    private List<MaterialAndData> fillBlocks = Collections.emptyList();
    private List<MaterialAndData> rimBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        shaftSize = IntegerRange.fromConfig(getLogger(), config, "size", 3, 3);
        shaftHeight = IntegerRange.fromConfig(getLogger(), config, "height", 2, 4);
        position = IntegerRange.fromConfig(getLogger(), config, "position", 8, 8);
        rimSize = IntegerRange.fromConfig(getLogger(), config, "rim_size", 0, 0);
        yOffset = IntegerRange.fromConfig(getLogger(), config, "y_offset", 0, 0);
        floorProbability = config.getDouble("floor_probability", 0);
        wallBlocks = parseBlocks(config, "blocks", "stones");
        fillBlocks = parseBlocks(config, "fill", "air");
        rimBlocks = parseBlocks(config, "rim", wallBlocks);
        return shaftSize.getMax() > 0;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final int centerX = position.getRandom(random);
        final int centerZ = position.getRandom(random);
        final int shaftWidth = this.shaftSize.getRandom(random);
        final int shaftHeight = this.shaftHeight.getRandom(random);
        final int rimSize = this.rimSize.getRandom(random);
        final int yOffset = this.yOffset.getRandom(random);
        final boolean isFloor = random.nextDouble() < floorProbability;
        final BlockData wallBlock = RandomUtils.getRandom(wallBlocks, random).createBlockData();
        final BlockData fillBlock = RandomUtils.getRandom(fillBlocks, random).createBlockData();
        final int startY = context.getSolidFloorOrCeiling(isFloor, centerX, world.getBedrockLevel(), centerZ) + yOffset;
        final int direction = isFloor ? -1 : 1;
        final int stopY = isFloor ? Math.max(context.getMinHeight() + 1, startY - shaftHeight) : Math.min(context.getMaxHeight() - 1, startY + shaftHeight);
        // This includes the border, so it is width + 1 on both sizes
        final int start = centerX - (shaftWidth / 2) - 1;
        final int end = start + shaftWidth + 1;

        for (int x = start; x <= end; x++) {
            for (int z = start; z <= end; z++) {
                for (int y = startY; y != stopY; y += direction) {
                    if (x == start || z == start || x == end || z == end) {
                        context.setBlockData(x, y, z, wallBlock);
                    } else {
                        context.setBlockData(x, y, z, fillBlock);
                    }
                }
            }
        }

        // Build rims
        if (rimSize > 0) {
            final BlockData rimBlock = RandomUtils.getRandom(rimBlocks, random).createBlockData();
            for (int x = start - rimSize; x <= end + rimSize; x++) {
                for (int z = start - rimSize; z <= end + rimSize; z++) {
                    if (x < start || x > end || z < start || z > end) {
                        context.setBlockData(x, startY, z, rimBlock);
                        context.setBlockData(x, stopY - direction, z, rimBlock);
                    }
                }
            }
        }
    }
}
