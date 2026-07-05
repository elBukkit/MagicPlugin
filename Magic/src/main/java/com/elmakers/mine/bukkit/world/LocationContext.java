package com.elmakers.mine.bukkit.world;

import org.bukkit.Location;
import org.bukkit.World;

public class LocationContext extends WorldContext {
    private final Location location;

    public LocationContext(Location location) {
        super(location.getChunk().getX(), location.getChunk().getZ());
        this.location = location;
    }

    public World getWorld() {
        return location.getWorld();
    }

    @Override
    public long getSeed() {
        return getWorld().getSeed();
    }

    @Override
    public int getMaxHeight() {
        return getWorld().getMaxHeight();
    }

    @Override
    public int getMinHeight() {
        return getWorld().getMinHeight();
    }
}
