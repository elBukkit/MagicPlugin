package com.elmakers.mine.bukkit.utility;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class TeleportPassengerTask implements Runnable {
    private final Entity vehicle;
    private final Entity passenger;
    private final Location location;

    public TeleportPassengerTask(final Entity vehicle, final Entity passenger, final Location location) {
        this.vehicle = vehicle;
        this.passenger = passenger;
        this.location = location;
    }

    @Override
    public void run() {
        CompatibilityUtils.teleportVehicle(passenger, location);
        CompatibilityUtils.addPassenger(vehicle, passenger);
    }
}
