package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.configuration.SpellParameters;

public class UpdateParametersAction extends CompoundAction
{
    @Override
    public SpellResult step(CastContext context) {
        if (handlers.isEmpty()) {
            context.getSpell().reloadParameters(context);
            return SpellResult.CAST;
        }
        ConfigurationSection parameters = context.getWorkingParameters();
        SpellParameters spellParameters = parameters instanceof SpellParameters ? (SpellParameters)parameters : null;
        CastContext originalContext = null;
        if (spellParameters != null) {
            originalContext = spellParameters.getContext();
            spellParameters.setContext(context);
        }
        for (ActionHandler handler : handlers.values()) {
            handler.prepare(context, parameters);
        }
        if (spellParameters != null) {
            spellParameters.setContext(originalContext);
        }
        return startActions();
    }
}
