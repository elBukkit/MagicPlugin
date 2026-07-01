package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class FrequencyPopulator extends BaseBlockPopulator {
    private BaseBlockPopulator populator;
    private int frequency = 2;

    public boolean onLoad(ConfigurationSection config) {
        frequency = config.getInt("frequency", frequency);
        populator = BaseBlockPopulator.parsePopulator(world, config);
        return populator != null;
    }

    protected boolean isValid(int chunkX, int chunkZ) {
        return chunkX % frequency == 0 && chunkZ % frequency == 0;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        if (isValid(chunkX, chunkZ)) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        if (isValid(location.getChunk().getX(), location.getChunk().getZ())) {
            return populator.getPortalTargetWorld(location);
        }
        return null;
    }
}
