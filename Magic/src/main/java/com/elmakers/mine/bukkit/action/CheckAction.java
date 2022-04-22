package com.elmakers.mine.bukkit.action;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public abstract class CheckAction extends CompoundAction {
    private boolean invert;
    private int timeout;
    private Long targetTime;

    protected abstract boolean isAllowed(CastContext context);

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        invert = parameters.getBoolean("invert", false);
        timeout = parameters.getInt("timeout", 0);
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        targetTime = null;
    }

    @Override
    public SpellResult start(CastContext context) {
        if (timeout > 0 && targetTime == null) {
            targetTime = System.currentTimeMillis() + timeout;
        }
        return super.start(context);
    }

    @Override
    protected void addHandlers(Spell spell, ConfigurationSection parameters) {
        addHandler(spell, "actions");
        addHandler(spell, "fail");
    }

    @Override
    public SpellResult step(CastContext context) {
        boolean allowed = isAllowed(context);
        if (invert) {
            allowed = !allowed;
        }
        if (!allowed) {
            if (targetTime != null && System.currentTimeMillis() < targetTime) {
                return SpellResult.PENDING;
            }
            ActionHandler fail = getHandler("fail");
            if (fail != null && fail.size() != 0) {
                return startActions("fail");
            }
        }
        ActionHandler actions = getHandler("actions");
        if (actions == null || actions.size() == 0) {
            return allowed ? SpellResult.CAST : SpellResult.STOP;
        }

        if (!allowed) {
            return SpellResult.NO_TARGET;
        }
        return startActions();
    }
}
