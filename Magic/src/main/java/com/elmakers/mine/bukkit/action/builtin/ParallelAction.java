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
    private List<SubAction> processing = null;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        ActionHandler actions = getHandler("actions");
        if (actions != null)
        {
            if (remaining == null) {
                remaining = new ArrayList<>();
            }
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

        if (processing == null) {
            processing = new ArrayList<>(remaining);
        } else {
            processing.clear();
            processing.addAll(remaining);
        }
        remaining.clear();

        int startingWork = context.getWorkAllowed();
        context.setWorkAllowed(0);
        int splitWork = Math.max(1, startingWork / processing.size());
        for (SubAction action : processing) {
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

    @Override
    public Object clone()
    {
        ParallelAction action = (ParallelAction)super.clone();
        // Don't share these lists
        action.remaining = null;
        action.processing = null;
        return action;
    }
}
