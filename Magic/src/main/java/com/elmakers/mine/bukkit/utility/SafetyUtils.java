package com.elmakers.mine.bukkit.utility;

import de.slikey.effectlib.util.MathUtils;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class SafetyUtils {
    public static void setVelocity(Entity entity, Vector velocity) {
        if (!MathUtils.isFinite(velocity.getX()) || !MathUtils.isFinite(velocity.getY()) || !MathUtils.isFinite(velocity.getZ())) {
            return;
        }
        if (Math.abs(velocity.getX()) > 10) velocity.setX(10 * Math.signum(velocity.getX()));
        if (Math.abs(velocity.getY()) > 10) velocity.setY(10 * Math.signum(velocity.getY()));
        if (Math.abs(velocity.getZ()) > 10) velocity.setZ(10 * Math.signum(velocity.getZ()));
        entity.setVelocity(velocity);
    }
}
