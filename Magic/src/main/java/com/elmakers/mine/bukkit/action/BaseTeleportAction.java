package com.elmakers.mine.bukkit.action;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public abstract class BaseTeleportAction extends BaseSpellAction
{
    protected int verticalSearchDistance;
    protected boolean safe = true;
    private boolean requiresBuildPermission = false;
    private boolean requiresExitPermission = true;
    private boolean keepVelocity = false;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        verticalSearchDistance = parameters.getInt("vertical_range", context.getVerticalSearchDistance());
        safe = parameters.getBoolean("safe", true);
        requiresBuildPermission = parameters.getBoolean("require_build", false);
        requiresExitPermission = parameters.getBoolean("require_exit", true);
        keepVelocity = parameters.getBoolean("keep_velocity", keepVelocity);
    }

    protected SpellResult teleport(CastContext context, Entity entity, Location targetLocation) {
        if (!CompatibilityUtils.checkChunk(targetLocation)) {
            return SpellResult.PENDING;
        }
        if (requiresBuildPermission && !context.hasBuildPermission(targetLocation.getBlock())) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (requiresExitPermission && entity instanceof Player && !context.getController().isExitAllowed((Player)entity, entity.getLocation()) && context.getController().isExitAllowed((Player)entity, targetLocation)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!context.canCast(targetLocation)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        Location sourceLocation = keepVelocity ? entity.getLocation() : null;
        Vector sourceVelocity = keepVelocity ? entity.getVelocity() : null;
        boolean result = context.teleport(
                entity, targetLocation, verticalSearchDistance, safe);
        if (result && keepVelocity) {
            // Calculate how much we have rotated
            double deltaYaw = targetLocation.getYaw() - sourceLocation.getYaw();
            double angle = Math.toRadians(deltaYaw);

            // Rotate the original velocity
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double initialX = sourceVelocity.getX();
            double initialZ = sourceVelocity.getZ();
            sourceVelocity.setX(initialX * cos - initialZ * sin);
            sourceVelocity.setZ(initialZ * cos + initialX * sin);

            // Re-apply to the entity
            entity.setVelocity(sourceVelocity);
        }

        return result ? SpellResult.CAST : SpellResult.NO_TARGET;
    }
}
