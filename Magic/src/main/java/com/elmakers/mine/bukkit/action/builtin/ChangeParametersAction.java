package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

@Deprecated
public class ChangeParametersAction extends BaseSpellAction {
    private ConfigurationSection parameters;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        this.parameters = parameters;
    }

    @Override
    public SpellResult perform(CastContext context) {
        context.setSpellParameters(parameters);
        return SpellResult.CAST;
    }
}
