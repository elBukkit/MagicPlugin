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
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class VinesPopulator extends BaseBlockPopulator {
    private boolean consistent = false;
    private int searchY = 16;
    private int minHeight = 0;
    private int maxHeight = 16;
    private int minPosition = 0;
    private int maxPosition = 14;

    private List<MaterialAndData> vineBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        consistent = config.getBoolean("consistent", consistent);
        vineBlocks = parseBlocks(config, "vines", "all_vines");
        minPosition = config.getInt("min_x", minPosition);
        maxPosition = config.getInt("max_x", maxPosition);
        minHeight = config.getInt("min_height", minHeight);
        maxHeight = config.getInt("max_height", maxHeight);
        searchY = config.getInt("search_y", searchY);
        return true;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int groundLevel = world.getGroundLevel();
        final int height = RandomUtils.range(random, minHeight, maxHeight);
        final int startX = RandomUtils.range(random, minPosition, maxPosition);
        final int startZ = RandomUtils.range(random, minPosition, maxPosition);
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;

        BlockData vineData = null;

        final int x = chunkGlobalX + startX;
        final int z = chunkGlobalZ + startZ;
        int groundY = getTopBlock(worldInfo, region, x, groundLevel, z);
        for (int dy = 0; dy < height; dy++) {
            // TODO: From ceiling down
            final int y = groundY + 1 + dy;
            if (vineData == null) {
                MaterialAndData vineMaterial = RandomUtils.getRandom(vineBlocks, random);
                vineData = vineMaterial.createBlockData();
            }
            region.setBlockData(x, y, z, vineData);
            if (!consistent) {
                vineData = null;
            }
        }
    }
}
