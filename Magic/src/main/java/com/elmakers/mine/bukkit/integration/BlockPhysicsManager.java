package com.elmakers.mine.bukkit.integration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.utility.DeprecatedUtils;

import uk.thinkofdeath.minecraft.physics.PhysicsPlugin;
import uk.thinkofdeath.minecraft.physics.api.PhysicsAPI;
import uk.thinkofdeath.minecraft.physics.api.PhysicsBlock;

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
        PhysicsBlock block = api.spawnBlock(location, DeprecatedUtils.newMaterialData(material, (byte)data));
        if (velocity != null) {
            block.applyForce(velocity.multiply(velocityScale));
        }
    }

    public void setVelocityScale(double scale) {
        this.velocityScale = scale;
    }
}
