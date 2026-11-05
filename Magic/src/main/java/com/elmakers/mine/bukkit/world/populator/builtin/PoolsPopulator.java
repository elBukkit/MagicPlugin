package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class PoolsPopulator extends BaseBlockPopulator {
    private IntegerRange poolsSize;
    private IntegerRange depth;
    private IntegerRange walkwayWidth;
    private double islandProbability = 0.75;
    private double lightProbability = 1;
    private IntegerRange position;
    private List<MaterialAndData> lightBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        poolsSize = IntegerRange.fromConfig(getLogger(), config, "size", 14, 14);
        depth = IntegerRange.fromConfig(getLogger(), config, "depth", 1, 1);
        walkwayWidth = IntegerRange.fromConfig(getLogger(), config, "walkway_width", 0, 10);
        islandProbability = config.getDouble("island_probability", islandProbability);
        lightProbability = config.getDouble("light_probability", lightProbability);
        lightBlocks = parseBlocks(config, "light_blocks", "all_lights");
        position = IntegerRange.fromConfig(getLogger(), config, "position", 8, 8);
        return true;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final int floorLevel = world.getGroundLevel();
        final int waterMinY = floorLevel - depth.getRandom(random);
        final int walkwayWidthHalf = walkwayWidth.getRandom(random);
        final int centerX = position.getRandom(random);
        final int centerZ = position.getRandom(random);
        final int walkwayLeftX = centerX - walkwayWidthHalf;
        final int walkWayRightX = centerX + walkwayWidthHalf;
        final int walkwayLeftZ = centerZ - walkwayWidthHalf;
        final int walkWayRightZ = centerZ + walkwayWidthHalf;
        final boolean hasIsland = random.nextDouble() < islandProbability;
        final int width = poolsSize.getRandom(random);
        final int lightsFirstX = walkwayLeftX / 2 + 1;
        final int lightsFirstZ = walkwayLeftZ / 2 + 1;
        final int lightsSecondX = width - lightsFirstX;
        final int lightsSecondZ = width - lightsFirstZ;
        final BlockData lightBlock = RandomUtils.getRandom(lightBlocks, random).createBlockData();
        final int startX = centerX - (width / 2);
        final int stopX = startX + width;
        final int startZ = centerZ - (width / 2);
        final int stopZ = startZ + width;

        for (int x = startX; x < stopX; x++) {
            for (int z = startZ; z < stopZ; z++) {
                if (!context.getType(x, floorLevel + 1, z).isAir()) continue;

                final BlockData waterBlock = Material.WATER.createBlockData();
                final boolean isWalkway = (x > walkwayLeftX && x < walkWayRightX) || (z > walkwayLeftZ && z < walkWayRightZ);
                final boolean isIsland = hasIsland && x >= 7 && z >= 7 && x <= 9 && z <= 9;

                if (!isWalkway && !isIsland) {
                    final boolean isCenterLight = (x == lightsFirstX || x == lightsSecondX) && (z == lightsFirstZ || z == lightsSecondZ);
                    if (isCenterLight && random.nextDouble() < lightProbability) {
                        context.setBlockData(x, waterMinY, z, lightBlock);
                    }

                    for (int y = floorLevel; y > waterMinY; y--) {
                        context.setBlockData(x, y, z, waterBlock);
                    }
                }
            }
        }
    }
}
