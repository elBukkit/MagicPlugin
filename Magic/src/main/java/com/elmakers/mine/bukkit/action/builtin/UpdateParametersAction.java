package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class UpdateParametersAction extends CompoundAction
{
    @Override
    public SpellResult step(CastContext context) {
        for (ActionHandler handler : handlers.values()) {
            handler.prepare(context, context.getWorkingParameters());
        }
        return startActions();
    }
}
