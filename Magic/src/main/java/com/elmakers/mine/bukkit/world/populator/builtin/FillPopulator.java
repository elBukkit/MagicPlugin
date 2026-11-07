package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.SearchType;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class FillPopulator extends BaseBlockPopulator {
    private IntegerRange sizeX;
    private IntegerRange sizeZ;
    private IntegerRange height;
    private IntegerRange positionX;
    private IntegerRange positionY;
    private IntegerRange positionZ;
    private SearchType searchType = SearchType.AREA_UP_MIN;
    private List<MaterialAndData> blocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        if (config.contains("position")) {
            positionX = IntegerRange.fromConfig(getLogger(), config, "position", 8, 8);
            positionZ = positionX;
        } else {
            positionX = IntegerRange.fromConfig(getLogger(), config, "position_x", 8, 8);
            positionZ = IntegerRange.fromConfig(getLogger(), config, "position_z", 8, 8);
        }
        if (config.contains("size")) {
            sizeX = IntegerRange.fromConfig(getLogger(), config, "size", 0, 0);
            sizeZ = sizeX;
        } else {
            sizeX = IntegerRange.fromConfig(getLogger(), config, "size_x", 0, 0);
            sizeZ = IntegerRange.fromConfig(getLogger(), config, "size_z", 0, 0);
        }
        if (config.contains("position_y")) {
            positionY = IntegerRange.fromConfig(getLogger(), config, "position_y", 0, 0);
        }
        height = IntegerRange.fromConfig(getLogger(), config, "height", 2, 4);
        blocks = parseBlocks(config, "blocks", "stone");
        searchType = ConfigUtils.parseEnum(config, "search", SearchType.AREA_UP_MIN, getLogger());
        return sizeX.getMax() > 0 && height.getMax() > 0 && !blocks.isEmpty();
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final int floorLevel = world.getGroundLevel();
        final int widthX = this.sizeX.getRandom(random);
        final int widthZ = this.sizeZ.getRandom(random);
        final int positionX = this.positionX.getRandom(random);
        final boolean fixedPosition = this.positionY != null;
        final int positionY = fixedPosition ? this.positionY.getRandom(random) : floorLevel;
        final int positionZ = this.positionZ.getRandom(random);
        final int height = this.height.getRandom(random);
        final int minX = positionX;
        final int maxX = positionX + widthX;
        final int minZ = positionZ;
        final int maxZ = positionZ + widthZ;
        final BlockData block = RandomUtils.getRandom(blocks, random).createBlockData();
        final int startY = context.searchArea(searchType, minX, positionY, minZ, maxX, maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                final int groundY = context.continueSearchArea(searchType, x, startY, z, positionY);
                final int maxY = Math.min(groundY + height, context.getMaxHeight() - 1);
                for (int y = groundY + 1; y <= maxY; y++) {
                    context.setBlockData(x, y, z, block);
                }
            }
        }
    }
}
