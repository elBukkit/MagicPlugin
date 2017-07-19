package com.elmakers.mine.bukkit.utility;

import de.slikey.effectlib.util.MathUtils;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class SafetyUtils {
    public static double MAX_VELOCITY = 10;

    public static void setVelocity(Entity entity, Vector velocity) {
        if (!MathUtils.isFinite(velocity.getX()) || !MathUtils.isFinite(velocity.getY()) || !MathUtils.isFinite(velocity.getZ())) {
            return;
        }
        if (Math.abs(velocity.getX()) > MAX_VELOCITY) velocity.setX(MAX_VELOCITY * Math.signum(velocity.getX()));
        if (Math.abs(velocity.getY()) > MAX_VELOCITY) velocity.setY(MAX_VELOCITY * Math.signum(velocity.getY()));
        if (Math.abs(velocity.getZ()) > MAX_VELOCITY) velocity.setZ(MAX_VELOCITY * Math.signum(velocity.getZ()));
        entity.setVelocity(velocity);
    }
}
