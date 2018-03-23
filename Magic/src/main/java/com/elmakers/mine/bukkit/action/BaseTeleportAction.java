package com.elmakers.mine.bukkit.action;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public abstract class BaseTeleportAction extends BaseSpellAction
{
    protected int verticalSearchDistance;
    protected boolean safe = true;
    private boolean requiresBuildPermission = false;
    private boolean requiresExitPermission = true;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        verticalSearchDistance = parameters.getInt("vertical_range", context.getVerticalSearchDistance());
        safe = parameters.getBoolean("safe", true);
        requiresBuildPermission = parameters.getBoolean("require_build", false);
        requiresExitPermission = parameters.getBoolean("require_exit", true);
    }

    protected SpellResult teleport(CastContext context, Entity entity, Location targetLocation) {
        if (requiresBuildPermission && !context.hasBuildPermission(targetLocation.getBlock())) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (requiresExitPermission && entity instanceof Player && !context.getController().isExitAllowed((Player)entity, entity.getLocation()) && context.getController().isExitAllowed((Player)entity, targetLocation)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!context.canCast(targetLocation)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        return context.teleport(entity, targetLocation, verticalSearchDistance, safe) ? SpellResult.CAST : SpellResult.NO_TARGET;
    }
}
