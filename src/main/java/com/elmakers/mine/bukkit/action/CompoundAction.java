package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.configuration.ConfigurationSection;

public abstract class CompoundAction extends DelayedCompoundAction
{
    @Override
    public void finish(CastContext context) {
        actions.finish(context);
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        actions.prepare(context, parameters);
    }
}
