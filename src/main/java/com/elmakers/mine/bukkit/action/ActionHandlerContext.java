package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import com.elmakers.mine.bukkit.api.action.CastContext;
import org.bukkit.entity.Entity;

public class ActionHandlerContext {
    private final ActionHandler actions;
    private final CastContext context;
    private final ConfigurationSection parameters;
    private final String messageKey;

    public ActionHandlerContext(ActionHandler handler, CastContext context, ConfigurationSection parameters, String messageKey) {
        this.actions = handler;
        this.context = context;
        this.parameters = parameters;
        this.messageKey = messageKey;
    }

    public SpellResult perform() {
        return perform(context);
    }

    public SpellResult perform(Entity sourceEntity, Location sourceLocation, Entity targetEntity, Location targetLocation) {
        CastContext newContext = new com.elmakers.mine.bukkit.action.CastContext(context, sourceEntity, sourceLocation);
        newContext.setTargetEntity(targetEntity);
        newContext.setTargetLocation(targetLocation);
        return perform(newContext);
    }

    public SpellResult perform(Entity sourceEntity, Location sourceLocation) {
        return perform(new com.elmakers.mine.bukkit.action.CastContext(context, sourceEntity, sourceLocation));
    }

    public SpellResult perform(CastContext context) {
        SpellResult result = this.actions.perform(context, parameters);
        if (messageKey != null) {
            context.messageTargets(messageKey);
        }
        return result;
    }

    public CastContext getContext() {
        return context;
    }
}
