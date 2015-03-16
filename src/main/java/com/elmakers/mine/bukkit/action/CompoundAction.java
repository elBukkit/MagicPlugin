package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
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

    public void reset(CastContext context)
    {
        super.reset(context);
        actions.reset(context);
    }

    protected SpellResult performActions(CastContext context) {
        if (actions == null) {
            return SpellResult.FAIL;
        }
        return actions.perform(context);
    }
}
