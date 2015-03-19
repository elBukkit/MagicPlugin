package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

public abstract class CompoundAction extends ParallelCompoundAction
{
    @Override
    public void finish(CastContext context) {
        if (actions != null) {
            actions.finish(getActionContext(context));
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        if (actions != null) {
            actions.prepare(getActionContext(context), parameters);
        }
    }

    public void reset(CastContext context)
    {
        super.reset(context);
        if (actions != null) {
            actions.reset(getActionContext(context));
        }
    }

    protected SpellResult performActions(CastContext context) {
        if (actions == null) {
            return SpellResult.FAIL;
        }
        return actions.perform(context);
    }
}
