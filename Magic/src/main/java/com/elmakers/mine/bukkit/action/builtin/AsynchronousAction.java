package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class AsynchronousAction extends CompoundAction
{
    private boolean queued = false;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        queued = false;
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (queued) {
            return SpellResult.NO_ACTION;
        }
        ActionHandler handler = getHandler("actions");
        if (handler == null) {
            return SpellResult.NO_ACTION;
        }

        actionContext = new com.elmakers.mine.bukkit.action.CastContext(context);
        handler = (ActionHandler)handler.clone();
        handler.reset(actionContext);
        actionContext.addHandler(handler);
        queued = true;
        return SpellResult.PENDING;
    }
}
