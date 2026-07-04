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
    private IntegerRange position;
    private List<MaterialAndData> lightBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        poolsWidth = IntegerRange.fromConfig(getLogger(), config, "width", 14, 14);
        depth = IntegerRange.fromConfig(getLogger(), config, "depth", 1, 1);
        walkwayWidth = IntegerRange.fromConfig(getLogger(), config, "walkway_width", 0, 10);
        islandProbability = config.getDouble("island_probability", islandProbability);
        lightProbability = config.getDouble("light_probability", lightProbability);
        lightBlocks = parseBlocks(config, "light_blocks", "all_lights");
        position = IntegerRange.fromConfig(getLogger(), config, "position", 8, 8);
        return true;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final boolean isStartingChunk = chunkX == 0 && chunkZ == 0;
        final int floorLevel = world.getGroundLevel();
        final int waterMinY = floorLevel - depth.getRandom(random);
        final int walkwayWidthHalf = isStartingChunk ? 0 : walkwayWidth.getRandom(random);
        final int centerX = position.getRandom(random);
        final int centerZ = position.getRandom(random);
        final int walkwayLeftX = centerX - walkwayWidthHalf;
        final int walkWayRightX = centerX + walkwayWidthHalf;
        final int walkwayLeftZ = centerZ - walkwayWidthHalf;
        final int walkWayRightZ = centerZ + walkwayWidthHalf;
        final boolean hasIsland = !isStartingChunk && random.nextDouble() < islandProbability;
        final int width = poolsWidth.getRandom(random);
        final int lightsFirstX = walkwayLeftX / 2 + 1;
        final int lightsFirstZ = walkwayLeftZ / 2 + 1;
        final int lightsSecondX = width - lightsFirstX;
        final int lightsSecondZ = width - lightsFirstZ;
        final BlockData lightBlock = RandomUtils.getRandom(lightBlocks, random).createBlockData();
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int startX = centerX - (width / 2);
        final int stopX = startX + width;
        final int startZ = centerZ - (width / 2);
        final int stopZ = startZ + width;

        for (int dx = startX; dx < stopX; dx++) {
            for (int dz = startZ; dz < stopZ; dz++) {
                final int x = chunkGlobalX + dx;
                final int z = chunkGlobalZ + dz;
                if (!region.getType(x, floorLevel + 1, z).isAir()) continue;

                final BlockData waterBlock = Material.WATER.createBlockData();
                final boolean isWalkway = (dx > walkwayLeftX && dx < walkWayRightX) || (dz > walkwayLeftZ && dz < walkWayRightZ);
                final boolean isIsland = hasIsland && dx >= 7 && dz >= 7 && dx <= 9 && dz <= 9;

                if (!isWalkway && !isIsland) {
                    final boolean isCenterLight = (dx == lightsFirstX || dx == lightsSecondX) && (dz == lightsFirstZ || dz == lightsSecondZ);
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
