package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.List;

import com.elmakers.mine.bukkit.action.ActionContext;
import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ParallelAction extends CompoundAction
{
    private List<ActionContext> remaining = null;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        ActionHandler actions = getHandler("actions");
        if (actions != null)
        {
            remaining = new ArrayList<>(actions.getActions());
            for (ActionContext action : remaining) {
                action.getAction().reset(context);
            }
        }
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        ActionHandler actions = getHandler("actions");
        if (actions != null)
        {
            actions.finish(context);
        }
    }

    @Override
	public SpellResult perform(CastContext context) {
        SpellResult result = SpellResult.NO_ACTION;
        int startingWork = context.getWorkAllowed();
        List<ActionContext> subActions = new ArrayList<>(remaining);
        remaining.clear();
        context.setWorkAllowed(0);
        int splitWork = Math.max(1, startingWork / subActions.size());
        for (ActionContext action : subActions) {
            context.setWorkAllowed(context.getWorkAllowed() + splitWork);
            SpellResult actionResult = action.perform(context);
            context.addResult(actionResult);
            if (actionResult.isStop()) {
                remaining.add(action);
            }
            result = result.min(actionResult);
        }

		return result;
	}
}
