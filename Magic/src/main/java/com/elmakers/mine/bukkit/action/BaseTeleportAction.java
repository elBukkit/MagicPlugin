package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public abstract class BaseTeleportAction extends BaseSpellAction
{
    protected int verticalSearchDistance;
    protected boolean safe = true;
    private boolean requiresBuildPermission = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        verticalSearchDistance = parameters.getInt("vertical_range", context.getVerticalSearchDistance());
        safe = parameters.getBoolean("safe", true);
        requiresBuildPermission = parameters.getBoolean("require_build", false);
    }

    protected SpellResult teleport(CastContext context, Entity entity, Location targetLocation) {
        if (requiresBuildPermission && !context.hasBuildPermission(targetLocation.getBlock())) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        if (!context.canCast(targetLocation)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        return context.teleport(entity, targetLocation, verticalSearchDistance, safe) ? SpellResult.CAST : SpellResult.FAIL;
    }
}
