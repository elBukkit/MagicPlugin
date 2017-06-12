package com.elmakers.mine.bukkit.utility;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class SafetyUtils {

    /**
     * Compatiblity method for Double.isFinite from Java 1.7 and up
     */
    static public boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    public static void setVelocity(Entity entity, Vector velocity) {
        if (!isFinite(velocity.getX()) || !isFinite(velocity.getY()) || !isFinite(velocity.getZ())) {
            return;
        }
        if (Math.abs(velocity.getX()) > 10) velocity.setX(10 * Math.signum(velocity.getX()));
        if (Math.abs(velocity.getY()) > 10) velocity.setY(10 * Math.signum(velocity.getY()));
        if (Math.abs(velocity.getZ()) > 10) velocity.setZ(10 * Math.signum(velocity.getZ()));
        entity.setVelocity(velocity);
    }
}
