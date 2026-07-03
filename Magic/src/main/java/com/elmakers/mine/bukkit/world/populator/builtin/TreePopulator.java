package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.SearchType;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

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
        baseBlocks = parseBlocks(config, "base", "dirt");
        searchType = ConfigUtils.parseEnum(config, "search_type", SearchType.AREA_UP_MAX, getLogger());
        return !treeTypes.isEmpty();
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int groundLevel = world.getGroundLevel();
        final MaterialAndData baseBlock = RandomUtils.getRandom(baseBlocks, random);
        final TreeType treeType = RandomUtils.getRandom(treeTypes, random);
        final int width = this.width.getRandom(random);
        final int startX = position.getRandom(random);
        final int startZ = position.getRandom(random);
        final int endX = startX + width;
        final int endZ = startZ + width;
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int startY = searchArea(searchType, worldInfo, region, startX + chunkGlobalX, groundLevel, startZ + chunkGlobalZ, endX + chunkGlobalX, endZ + chunkGlobalZ);
        if (baseBlock != null) {
            for (int dx = startX; dx < endX; dx++) {
                for (int dz = startZ; dz < endZ; dz++) {
                    final int x = chunkGlobalX + dx;
                    final int z = chunkGlobalZ + dz;
                    final int groundY = continueSearchArea(searchType, worldInfo, region, x, startY, z, groundLevel);
                    region.setBlockData(x, groundY, z, baseBlock.createBlockData());
                }
            }
        }
        final int x = chunkGlobalX + startX;
        final int z = chunkGlobalZ + startZ;
        region.generateTree(new Location(world.getWorld(), x, startY, z), random, treeType);
    }
}
