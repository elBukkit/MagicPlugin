package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class FloodingPopulator extends BaseBlockPopulator {
    private IntegerRange floodingLevel;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        floodingLevel = IntegerRange.fromConfig(getLogger(), config, "flooding_level", 1, 6);
        return true;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final MagicController controller = getController();
        final int floorLevel = world.getGroundLevel();
        int floodLevel = floodingLevel.getRandom(random);
        Levelled floodWater = (Levelled)controller.getPlugin().getServer().createBlockData(Material.WATER);
        floodWater.setLevel(floodLevel);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (!context.getType(x, floorLevel, z).isSolid()) continue;
                if (!context.getType(x, floorLevel + 1, z).isAir()) continue;
                context.setBlockData(x, floorLevel + 1, z, floodWater);
            }
        }
    }
}
