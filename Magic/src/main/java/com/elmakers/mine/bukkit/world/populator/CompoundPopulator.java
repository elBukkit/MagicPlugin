package com.elmakers.mine.bukkit.world.populator;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

public abstract class CompoundPopulator extends BaseBlockPopulator {

    protected abstract List<BaseBlockPopulator> getPopulators(long worldSeed, int chunkX, int chunkZ);

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        for (BaseBlockPopulator populator : getPopulators(worldInfo.getSeed(), chunkX, chunkZ)) {
            populator.populate(worldInfo, random, chunkX, chunkZ, region);
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        for (BaseBlockPopulator populator : getPopulators(location.getWorld().getSeed(), location.getChunk().getX(), location.getChunk().getZ())) {
            String targetWorld = populator.getPortalTargetWorld(location);
            if (targetWorld != null) {
                return targetWorld;
            }
        }
        return null;
    }
}
