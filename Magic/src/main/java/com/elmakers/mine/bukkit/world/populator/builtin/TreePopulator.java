package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.TreeType;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.SearchType;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class TreePopulator extends BaseBlockPopulator {
    private IntegerRange position;
    private IntegerRange width;
    private SearchType searchType = SearchType.AREA_UP_MAX;

    private List<TreeType> treeTypes = Collections.emptyList();
    private List<MaterialAndData> baseBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        treeTypes = ConfigUtils.parseEnumList(config, "types", TreeType.BIRCH, getLogger());
        position = IntegerRange.fromConfig(getLogger(), config, "position", 0, 14);
        width = IntegerRange.fromConfig(getLogger(), config, "width", 1, 3);
        baseBlocks = parseBlocks(config, "base");
        searchType = ConfigUtils.parseEnum(config, "search", SearchType.AREA_UP_MAX, getLogger());
        return !treeTypes.isEmpty();
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final int groundLevel = world.getGroundLevel();
        final MaterialAndData baseBlock = RandomUtils.getRandom(baseBlocks, random);
        final TreeType treeType = RandomUtils.getRandom(treeTypes, random);
        final int width = this.width.getRandom(random);
        final int centerX = position.getRandom(random);
        final int centerZ = position.getRandom(random);
        final int startX = centerX - width / 2;
        final int startZ = centerZ - width / 2;
        final int endX = startX + width;
        final int endZ = startZ + width;
        final int startY = context.searchArea(searchType, startX, groundLevel, startZ, endX, endZ);
        if (baseBlock != null) {
            for (int x = startX; x < endX; x++) {
                for (int z = startZ; z < endZ; z++) {
                    final int groundY = context.continueSearchArea(searchType, x, startY, z, groundLevel);
                    context.setBlockData(x, groundY, z, baseBlock.createBlockData());
                }
            }
        }
        final int x = centerX;
        final int z = centerZ;
        final int y = 1 + context.getTopBlock(x, groundLevel, z);
        context.generateTree(x, y, z, random, treeType);
    }
}
