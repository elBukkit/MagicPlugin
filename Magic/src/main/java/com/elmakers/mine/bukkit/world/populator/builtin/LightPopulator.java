package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class LightPopulator extends BaseBlockPopulator {
    private IntegerRange position;
    private double floorProbability;
    private int searchY = 32;
    private List<MaterialAndData> lightBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        position = IntegerRange.fromConfig(getLogger(), config, "position", 0, 15);
        lightBlocks = parseBlocks(config, "light_blocks", "all_lights");
        floorProbability = config.getDouble("floor_probability", 0);
        searchY = config.getInt("search_y", searchY);
        return true;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final BlockData lightBlock = RandomUtils.getRandom(lightBlocks, random).createBlockData();
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int x = chunkGlobalX + position.getRandom(random);
        final int z = chunkGlobalZ + position.getRandom(random);
        final boolean isFloor = random.nextDouble() < floorProbability;
        final int topY = searchBlock(worldInfo, region, x, world.getBedrockLevel(), z, searchY, 1, true, true, material -> material.isSolid());
        final int y;
        if (isFloor) {
            y = topY;
        } else {
            y = searchBlock(worldInfo, region, x, topY + 1, z, searchY, 1, true, false);
        }
        region.setBlockData(x, y, z, lightBlock);
    }
}
