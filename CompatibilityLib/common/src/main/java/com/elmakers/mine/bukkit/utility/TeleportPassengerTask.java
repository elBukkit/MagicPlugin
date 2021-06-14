package com.elmakers.mine.bukkit.utility;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class TeleportPassengerTask implements Runnable {
    private final CompatibilityUtils compatibilityUtils;
    private final Entity vehicle;
    private final Entity passenger;
    private final Location location;

    public TeleportPassengerTask(final CompatibilityUtils compatibilityUtils, final Entity vehicle, final Entity passenger, final Location location) {
        this.compatibilityUtils = compatibilityUtils;
        this.vehicle = vehicle;
        this.passenger = passenger;
        this.location = location;
    }

    @Override
    public void run() {
        compatibilityUtils.teleportVehicle(passenger, location);
        compatibilityUtils.addPassenger(vehicle, passenger);
    }
}
