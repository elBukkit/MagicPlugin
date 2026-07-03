package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class FloodingPopulator extends BaseBlockPopulator {
    private IntegerRange floodingLevel;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        floodingLevel = IntegerRange.fromConfig(getLogger(), config, "flooding_level", 1, 6);
        return true;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final MagicController controller = getController();
        final int floorLevel = world.getGroundLevel();
        int floodLevel = floodingLevel.getRandom(random);
        Levelled floodWater = (Levelled)controller.getPlugin().getServer().createBlockData(Material.WATER);
        floodWater.setLevel(floodLevel);
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                final int x = chunkGlobalX + dx;
                final int z = chunkGlobalZ + dz;

                if (!region.getType(x, floorLevel, z).isSolid()) continue;
                if (!region.getType(x, floorLevel + 1, z).isAir()) continue;
                region.setBlockData(x, floorLevel + 1, z, floodWater);
            }
        }
    }
}
