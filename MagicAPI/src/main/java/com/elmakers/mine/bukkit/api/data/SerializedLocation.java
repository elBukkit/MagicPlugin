package com.elmakers.mine.bukkit.api.data;

import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;

public class SerializedLocation {
    private static boolean spammed = false;

    private String worldName;
    private BlockVector location;
    private float yaw;
    private float pitch;

    public SerializedLocation(String worldName, BlockVector location, float yaw, float pitch) {
        this.worldName = worldName;
        this.location = location;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public SerializedLocation(Location location) {
        this.location = new BlockVector(location.toVector());
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        try {
            this.worldName = location.getWorld().getName();
        } catch (Exception ex) {
            if (!spammed) {
                Bukkit.getLogger().log(Level.SEVERE, "Error getting world from location: " + location, ex);
                spammed = true;
            }
        }
    }

    @Nullable
    public Location asLocation() {
        World world = getWorld();
        return world == null ? null : new Location(world, location.getX(), location.getY(), location.getZ(), yaw, pitch);
    }

    @Nullable
    public World getWorld() {
        if (worldName == null || worldName.isEmpty()) return null;
        World world = null;
        try {
            world = Bukkit.getWorld(worldName);
        } catch (Exception ex) {
            if (!spammed) {
                Bukkit.getLogger().log(Level.SEVERE, "Error getting world: " + worldName, ex);
                spammed = true;
            }
            world = null;
        }
        return world;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return location.getX();
    }

    public double getY() {
        return location.getY();
    }

    public double getZ() {
        return location.getZ();
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }
}
