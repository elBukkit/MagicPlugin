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
import com.elmakers.mine.bukkit.world.SearchType;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class FillPopulator extends BaseBlockPopulator {
    private IntegerRange widthX;
    private IntegerRange widthZ;
    private IntegerRange height;
    private IntegerRange positionX;
    private IntegerRange yOffset;
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
        if (config.contains("width")) {
            widthX = IntegerRange.fromConfig(getLogger(), config, "width", 0, 0);
            widthZ = widthX;
        } else {
            widthX = IntegerRange.fromConfig(getLogger(), config, "width_x", 0, 0);
            widthZ = IntegerRange.fromConfig(getLogger(), config, "width_z", 0, 0);
        }
        yOffset = IntegerRange.fromConfig(getLogger(), config, "y_offset", 0, 0);
        if (config.contains("position_y")) {
            positionY = IntegerRange.fromConfig(getLogger(), config, "position_y", 0, 0);
        }
        height = IntegerRange.fromConfig(getLogger(), config, "height", 2, 4);
        blocks = parseBlocks(config, "blocks", "stone");
        return widthX.getMax() > 0 && height.getMax() > 0 && !blocks.isEmpty();
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = world.getGroundLevel();
        final int widthX = this.widthX.getRandom(random);
        final int widthZ = this.widthZ.getRandom(random);
        final int positionX = this.positionX.getRandom(random);
        final int yOffset = this.yOffset.getRandom(random);
        final boolean fixedPosition = this.positionY != null;
        final int positionY = fixedPosition ? this.positionY.getRandom(random) : floorLevel;
        final int positionZ = this.positionZ.getRandom(random);
        final int height = this.height.getRandom(random);
        final int minX = chunkGlobalX + positionX;
        final int maxX = chunkGlobalX + positionX + widthX;
        final int minZ = chunkGlobalZ + positionZ;
        final int maxZ = chunkGlobalZ + positionZ + widthZ;
        final BlockData block = RandomUtils.getRandom(blocks, random).createBlockData();
        final int startY = searchArea(searchType, worldInfo, region, minX, positionY, minZ, maxX, maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                final int groundY = continueSearchArea(searchType, worldInfo, region, x, startY, z, positionY) + yOffset;
                final int maxY = Math.min(groundY + height, worldInfo.getMaxHeight() - 1);
                for (int y = groundY + 1; y <= maxY; y++) {
                    region.setBlockData(x, y, z, block);
                }
            }
        }
    }
}
