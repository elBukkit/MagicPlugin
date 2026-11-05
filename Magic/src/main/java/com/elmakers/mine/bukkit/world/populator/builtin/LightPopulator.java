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

public class LightPopulator extends BaseBlockPopulator {
    private IntegerRange position;
    private double floorProbability;
    private List<MaterialAndData> lightBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        position = IntegerRange.fromConfig(getLogger(), config, "position", 0, 15);
        lightBlocks = parseBlocks(config, "light_blocks", "all_lights");
        floorProbability = config.getDouble("floor_probability", 0);
        return true;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final BlockData lightBlock = RandomUtils.getRandom(lightBlocks, random).createBlockData();
        final int x = position.getRandom(random);
        final int z = position.getRandom(random);
        final boolean isFloor = random.nextDouble() < floorProbability;
        final int y = context.getSolidFloorOrCeiling(isFloor, x, world.getBedrockLevel(), z);
        context.setBlockData(x, y, z, lightBlock);
    }
}
