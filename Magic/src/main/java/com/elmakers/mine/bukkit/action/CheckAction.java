package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public abstract class CheckAction extends CompoundAction {

    protected abstract boolean isAllowed(CastContext context);

    @Override
    public SpellResult step(CastContext context) {
        boolean allowed = isAllowed(context);
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