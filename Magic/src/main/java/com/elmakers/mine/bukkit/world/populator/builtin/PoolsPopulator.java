package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class PoolsPopulator extends BaseBlockPopulator {
    private IntegerRange poolsWidth;
    private IntegerRange depth;
    private IntegerRange walkwayWidth;
    private double islandProbability = 0.75;
    private double lightProbability = 1;
    private List<MaterialAndData> lightBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        poolsWidth = IntegerRange.fromConfig(getLogger(), config, "width", 14, 14);
        depth = IntegerRange.fromConfig(getLogger(), config, "depth", 1, 1);
        walkwayWidth = IntegerRange.fromConfig(getLogger(), config, "walkway_width", 0, 10);
        islandProbability = config.getDouble("island_probability", islandProbability);
        lightProbability = config.getDouble("light_probability", lightProbability);
        lightBlocks = parseBlocks(config, "light_blocks", "all_lights");

        return true;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final boolean isStartingChunk = chunkX == 0 && chunkZ == 0;
        final int floorLevel = world.getGroundLevel();
        final int waterMinY = floorLevel - depth.getRandom(random);
        final int walkwayWidthHalf = isStartingChunk ? 0 : walkwayWidth.getRandom(random);
        final int walkwayLeft = 8 - walkwayWidthHalf;
        final int walkWayRight = 8 + walkwayWidthHalf;
        final boolean hasIsland = !isStartingChunk && random.nextDouble() < islandProbability;
        final int lightsFirst = walkwayLeft / 2 + 1;
        final int lightsSecond = 16 - lightsFirst;
        final BlockData lightBlock = RandomUtils.getRandom(lightBlocks, random).createBlockData();
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int width = poolsWidth.getRandom(random);
        final int start = 8 - (int)Math.floor((double)width / 2);
        final int stop = 8 + (int)Math.ceil((double)width / 2);

        for (int dx = start; dx < stop; dx++) {
            for (int dz = start; dz < stop; dz++) {
                final int x = chunkGlobalX + dx;
                final int z = chunkGlobalZ + dz;
                if (!region.getType(x, floorLevel + 1, z).isAir()) continue;

                final BlockData waterBlock = Material.WATER.createBlockData();
                final boolean isWalkway = (dx > walkwayLeft && dx < walkWayRight) || (dz > walkwayLeft && dz < walkWayRight);
                final boolean isIsland = hasIsland && dx >= 7 && dz >= 7 && dx <= 9 && dz <= 9;

                if (!isWalkway && !isIsland) {
                    final boolean isCenterLight = (dx == lightsFirst || dx == lightsSecond) && (dz == lightsFirst || dz == lightsSecond);
                    if (isCenterLight && random.nextDouble() < lightProbability) {
                        region.setBlockData(x, waterMinY, z, lightBlock);
                    }

                    for (int y = floorLevel; y > waterMinY; y--) {
                        region.setBlockData(x, y, z, waterBlock);
                    }
                }
            }
        }
    }
}
