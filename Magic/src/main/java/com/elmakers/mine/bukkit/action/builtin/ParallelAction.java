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
    private static class SubAction {
        private ActionContext action;
        private CastContext context;

        public SubAction(ActionContext action, CastContext context) {
            this.action = action;
            this.context = new com.elmakers.mine.bukkit.action.CastContext(context);
        }

        public SpellResult perform() {
            return action.perform(context);
        }
    }

    private List<SubAction> remaining = null;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        ActionHandler actions = getHandler("actions");
        if (actions != null)
        {
            remaining = new ArrayList<>();
            for (ActionContext action : actions.getActions()) {
                action.getAction().reset(context);
                remaining.add(new SubAction(action, context));
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

        if (remaining.isEmpty()) {
            return result;
        }

        List<SubAction> subActions = new ArrayList<>(remaining);
        remaining.clear();

        context.setWorkAllowed(0);
        int startingWork = context.getWorkAllowed();
        int splitWork = Math.max(1, startingWork / subActions.size());
        for (SubAction action : subActions) {
            context.setWorkAllowed(context.getWorkAllowed() + splitWork);
            SpellResult actionResult = action.perform();
            context.addResult(actionResult);
            if (actionResult.isStop()) {
                remaining.add(action);
            }
            result = result.min(actionResult);
        }

        return result;
    }
}
