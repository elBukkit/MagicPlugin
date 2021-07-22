package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;

@Deprecated
public class CreateAutomatonAction extends CreateMagicBlockAction {
    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        templateKey = parameters.getString("automaton");
        this.parameters = parameters.getConfigurationSection("automaton_parameters");
    }
}
