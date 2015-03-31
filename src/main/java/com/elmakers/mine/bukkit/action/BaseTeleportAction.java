package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.configuration.ConfigurationSection;

public abstract class BaseTeleportAction extends BaseSpellAction
{
    protected int verticalSearchDistance;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        verticalSearchDistance = parameters.getInt("vertical_range", context.getVerticalSearchDistance());
    }
}
