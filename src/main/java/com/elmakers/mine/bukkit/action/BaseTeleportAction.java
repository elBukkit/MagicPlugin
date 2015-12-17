package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public abstract class BaseTeleportAction extends BaseSpellAction
{
    protected int verticalSearchDistance;
    protected boolean safe = true;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        verticalSearchDistance = parameters.getInt("vertical_range", context.getVerticalSearchDistance());
        safe = parameters.getBoolean("safe", true);
    }

    protected void teleport(CastContext context, Entity entity, Location targetLocation) {
        context.teleport(entity, targetLocation, verticalSearchDistance, safe);
    }
}
