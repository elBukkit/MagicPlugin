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
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final int groundLevel = world.getGroundLevel();
        final int height = this.height.getRandom(random);
        final int startX = this.position.getRandom(random);
        final int startZ = this.position.getRandom(random);

        BlockData vineData = null;

        final int x = startX;
        final int z = startZ;
        final int groundY = context.getTopBlock(x, groundLevel, z);
        final int ceilingY = context.searchBlock(x, groundY + 1, z, searchY, 1, true, true);
        if (!context.isInRegion(x, ceilingY + 1, z) || !context.getType(x, ceilingY + 1, z).isSolid()) {
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

            context.setBlockData(x, y, z, vineData);
            if (!consistent) {
                vineData = null;
            }
        }
    }
}
