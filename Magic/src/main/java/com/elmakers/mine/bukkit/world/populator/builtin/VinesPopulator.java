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

public class VinesPopulator extends BaseBlockPopulator {
    private boolean consistent = false;
    private int searchY = 16;
    private IntegerRange position;
    private IntegerRange height;

    private List<MaterialAndData> vineBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        consistent = config.getBoolean("consistent", consistent);
        vineBlocks = parseBlocks(config, "vines", "all_vines");
        position = IntegerRange.fromConfig(getLogger(), config, "position", 0, 15);
        height = IntegerRange.fromConfig(getLogger(), config, "height", 4, 64);
        searchY = config.getInt("search_y", searchY);
        return true;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int groundLevel = world.getGroundLevel();
        final int height = this.height.getRandom(random);
        final int startX = this.position.getRandom(random);
        final int startZ = this.position.getRandom(random);
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;

        BlockData vineData = null;

        final int x = chunkGlobalX + startX;
        final int z = chunkGlobalZ + startZ;
        final int groundY = getTopBlock(worldInfo, region, x, groundLevel, z);
        final int ceilingY = searchBlock(worldInfo, region, x, groundY + 1, z, searchY, 1, true, true);
        if (!region.isInRegion(x, ceilingY + 1, z) || !region.getType(x, ceilingY + 1, z).isSolid()) {
            return;
        }

        for (int dy = 0; dy < height; dy++) {
            final int y = ceilingY - dy;
            if (y <= groundY) break;

            if (vineData == null) {
                MaterialAndData vineMaterial = RandomUtils.getRandom(vineBlocks, random);
                vineData = vineMaterial == null ? null : vineMaterial.createBlockData();
            }
            if (vineData == null) continue;

            region.setBlockData(x, y, z, vineData);
            if (!consistent) {
                vineData = null;
            }
        }
    }
}
