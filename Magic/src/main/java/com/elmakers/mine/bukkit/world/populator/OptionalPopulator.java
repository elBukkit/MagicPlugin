package com.elmakers.mine.bukkit.world.populator;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

public abstract class OptionalPopulator extends BaseBlockPopulator {
    private BaseBlockPopulator populator;
    private int frequency = 2;

    public boolean onLoad(ConfigurationSection config) {
        frequency = config.getInt("frequency", frequency);
        populator = BaseBlockPopulator.parsePopulator(world, config);
        return populator != null;
    }

    protected abstract BaseBlockPopulator getPopulator(long worldSeed, int chunkX, int chunkZ);

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        BaseBlockPopulator populator = getPopulator(worldInfo.getSeed(), chunkX, chunkZ);
        if (populator != null) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        String targetWorld = super.getPortalTargetWorld(location);
        if (targetWorld != null) {
            return targetWorld;
        }
        BaseBlockPopulator populator = getPopulator(location.getWorld().getSeed(), location.getChunk().getX(), location.getChunk().getZ());
        if (populator == null) {
            return null;
        }
        return populator.getPortalTargetWorld(location);
    }
}
