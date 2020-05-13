package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ModifyVariableAction extends BaseSpellAction {
    private String key;
    private double value;
    private boolean clear;

    private void checkDefaults(ConfigurationSection variables, ConfigurationSection parameters) {
        if (!variables.contains(key)) {
            double defaultValue = parameters.getDouble("default", 0);
            variables.set(key, defaultValue);
        }
    }

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        key = parameters.getString("variable", "");
        checkDefaults(spell.getVariables(), parameters);
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        key = parameters.getString("variable", "");
        checkDefaults(context.getVariables(), parameters);
        clear = parameters.getBoolean("clear");
        value = parameters.getDouble("value", 0);
    }

    @Override
    public SpellResult perform(CastContext context) {
        ConfigurationSection variables = context.getVariables();
        if (clear) {
            if (!variables.contains(key)) {
                return SpellResult.NO_TARGET;
            }
            variables.set(key, null);
        } else {
            if (variables.contains(key) && variables.getDouble(key) == value) {
                return SpellResult.NO_TARGET;
            }
            variables.set(key, value);
        }
        context.getSpell().reloadParameters();
        return SpellResult.CAST;
    }
}
