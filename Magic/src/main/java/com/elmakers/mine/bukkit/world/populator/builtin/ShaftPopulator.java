package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
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
    private double floorProbability;
    private List<MaterialAndData> wallBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        shaftWidth = IntegerRange.fromConfig(getLogger(), config, "width", 3, 3);
        shaftHeight = IntegerRange.fromConfig(getLogger(), config, "height", 2, 4);
        floorProbability = config.getDouble("floor_probability", 0);
        wallBlocks = parseBlocks(config, "blocks", "stones");
        return shaftWidth.getMax() > 0;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int centerX = 8;
        final int centerZ = 8;
        final int shaftWidth = this.shaftWidth.getRandom(random);
        final int shaftHeight = this.shaftHeight.getRandom(random);
        final boolean isFloor = random.nextDouble() < floorProbability;
        final BlockData wallBlock = RandomUtils.getRandom(wallBlocks, random).createBlockData();
        final int startY = getSolidFloorOrCeiling(isFloor, worldInfo, region, centerX, world.getBedrockLevel(), centerZ);
        final int direction = isFloor ? -1 : 1;
        final int stopY = isFloor ? Math.max(worldInfo.getMinHeight() + 1, startY - shaftHeight) : Math.min(worldInfo.getMaxHeight() - 1, startY + shaftHeight);
        final int start = 8 - (int)Math.ceil((double)shaftWidth / 2) - 1;
        final int end = 8 + (int)Math.floor((double)shaftWidth / 2) + 1;

        for (int dx = start; dx <= end; dx++) {
            for (int dz = start; dz <= end; dz++) {
                final int x = chunkGlobalX + dx;
                final int z = chunkGlobalZ + dz;
                for (int y = startY; y != stopY; y += direction) {
                    if (dx == start || dz == start || dx == end || dz == end) {
                        region.setBlockData(x, y, z, wallBlock);
                    } else {
                        region.setType(x, y, z, Material.AIR);
                    }
                }
            }
        }
    }
}
