package com.elmakers.mine.bukkit.utility.platform.base.populator;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

public class OutOfBoundsEntityCleanup extends BlockPopulator {
    private final Logger log;

    public OutOfBoundsEntityCleanup(Logger log) {
        this.log = log;
    }

    @Override
    public void populate(WorldInfo world, Random random, int x, int z, LimitedRegion region) {
        for (Entity entity : region.getEntities()) {
            Location location = entity.getLocation();
            if (!region.isInRegion(location)) {
                log.info("Removing out of bounds entity at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + " in " + location.getWorld().getName());
                entity.remove();
            }
        }
    }
}
