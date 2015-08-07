package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionContext;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

import java.util.ArrayList;
import java.util.List;

public class ParallelAction extends CompoundAction
{
    private List<ActionContext> remaining = null;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        remaining = new ArrayList<ActionContext>(actions.getActions());
        for (ActionContext action : remaining) {
            action.getAction().reset(context);
        }
    }

    @Override
	public SpellResult perform(CastContext context) {
        SpellResult result = SpellResult.NO_ACTION;
        int startingWork = context.getWorkAllowed();
        List<ActionContext> subActions = new ArrayList<ActionContext>(remaining);
        remaining.clear();
        context.setWorkAllowed(0);
        int splitWork = Math.max(1, startingWork / subActions.size());
        for (ActionContext action : subActions) {
            context.setWorkAllowed(context.getWorkAllowed() + splitWork);
            SpellResult actionResult = action.perform(context);
            if (actionResult.isStop()) {
                remaining.add(action);
            }
            result = result.min(actionResult);
        }

		return result;
	}
}
