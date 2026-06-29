package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class PoolsPopulator extends BaseBlockPopulator {
    private IntegerRange depth;
    private IntegerRange walkwayWidth;
    private double islandProbability = 0.75;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        depth = IntegerRange.fromConfig(getLogger(), config, "water_depth", 1, 1);
        walkwayWidth = IntegerRange.fromConfig(getLogger(), config, "walkway_width", 0, 10);
        islandProbability = config.getDouble("island_probability", islandProbability);
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
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;

        for (int dx = 1; dx < 15; dx++) {
            for (int dz = 1; dz < 15; dz++) {
                final int x = chunkGlobalX + dx;
                final int z = chunkGlobalZ + dz;
                if (!region.getType(x, floorLevel + 1, z).isAir()) continue;

                final BlockData waterBlock = Material.WATER.createBlockData();
                final boolean isWalkway = (dx > walkwayLeft && dx < walkWayRight) || (dz > walkwayLeft && dz < walkWayRight);
                final boolean isIsland = hasIsland && dx >= 7 && dz >= 7 && dx <= 9 && dz <= 9;

                if (!isWalkway && !isIsland) {
                    for (int y = floorLevel; y > waterMinY; y--) {
                        region.setBlockData(x, y, z, waterBlock);
                    }
                }
            }
        }
    }
}
