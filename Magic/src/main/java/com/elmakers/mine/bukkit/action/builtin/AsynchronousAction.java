package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class AsynchronousAction extends CompoundAction
{
    @Override
    public SpellResult perform(CastContext context) {
        ActionHandler handler = getHandler("actions");
        if (handler == null) {
            return SpellResult.NO_ACTION;
        }

        actionContext = new com.elmakers.mine.bukkit.action.CastContext(context);
        handler = (ActionHandler)handler.clone();
        handler.reset(actionContext);
        actionContext.addHandler(handler);
        return SpellResult.CAST;
    }
}
