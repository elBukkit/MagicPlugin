package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ActionHandlerContext {
    private final com.elmakers.mine.bukkit.api.action.ActionHandler actions;
    private final CastContext context;

    public ActionHandlerContext(com.elmakers.mine.bukkit.api.action.ActionHandler handler, CastContext context) {
        this.actions = handler;
        this.context = new com.elmakers.mine.bukkit.action.CastContext(context);
    }

    public SpellResult perform() {
        return actions.perform(context);
    }

    public void setWorkAllowed(int work) {
        context.setWorkAllowed(work);
    }

    public void finish() {
        actions.finish(context);
    }
}
