package com.elmakers.mine.bukkit.integration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import uk.thinkofdeath.minecraft.physics.PhysicsPlugin;
import uk.thinkofdeath.minecraft.physics.api.PhysicsAPI;

public class BlockPhysicsManager {
    private final PhysicsAPI api;
    private double velocityScale = 1;

    public BlockPhysicsManager(Plugin owningPlugin, Plugin physicsPlugin) {
        if (physicsPlugin instanceof PhysicsPlugin) {
            this.api = ((PhysicsPlugin)physicsPlugin).getAPI(owningPlugin);
        } else {
            this.api = null;
        }
    }

    public boolean isEnabled() {
        return this.api != null;
    }

    public void spawnPhysicsBlock(Location location, Material material, short data, Vector velocity) {
        if (api == null) return;
        // TODO: Time to remove this.
        /*
        PhysicsBlock block = api.spawnBlock(location, material, 0);
        if (velocity != null) {
            block.applyForce(velocity.multiply(velocityScale));
        }
        */
    }

    public void setVelocityScale(double scale) {
        this.velocityScale = scale;
    }
}
