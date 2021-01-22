package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ModifyParametersAction extends CompoundAction {
    private ConfigurationSection parameters;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        this.parameters = parameters;
    }

    @Override
    public SpellResult step(CastContext context) {
        context.setSpellParameters(parameters);
        ActionHandler handler = handlers.get("actions");
        if (handler != null) {
            handler.prepare(context, parameters);
        }
        return startActions();
    }
}
