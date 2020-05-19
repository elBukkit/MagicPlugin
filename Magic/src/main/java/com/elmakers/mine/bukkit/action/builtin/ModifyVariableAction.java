package com.elmakers.mine.bukkit.action.builtin;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.VariableScope;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ModifyVariableAction extends BaseSpellAction {
    private String key;
    private boolean hasValue;
    private double value;
    private boolean clear;
    private VariableScope scope = VariableScope.SPELL;

    private void checkDefaults(ConfigurationSection variables, ConfigurationSection parameters) {
        if (!clear && !variables.contains(key)) {
            double defaultValue = parameters.getDouble("default", 0);
            variables.set(key, defaultValue);
        } else if (clear && variables.contains(key)) {
            variables.set(key, null);
        }
    }

    private void parseScope(ConfigurationSection parameters, Logger logger) {
        String scopeString = parameters.getString("scope");
        if (scopeString != null && !scopeString.isEmpty()) {
            try {
                scope = VariableScope.valueOf(scopeString.toUpperCase());
            } catch (Exception ex) {
                logger.warning("Invalid variable scope: " + scopeString);
            }
        }
    }

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        key = parameters.getString("variable", "");
        clear = parameters.getBoolean("clear");
        parseScope(parameters, spell.getController().getLogger());
        switch (scope) {
            case SPELL:
                checkDefaults(spell.getVariables(), parameters);
            break;
            case MAGE:
                if (spell instanceof MageSpell) {
                    Mage mage = ((MageSpell)spell).getMage();
                    if (mage != null) {
                        checkDefaults(mage.getVariables(), parameters);
                    }
                }
            break;
            default: break;
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        parseScope(parameters, context.getLogger());
        key = parameters.getString("variable", "");
        clear = parameters.getBoolean("clear");
        checkDefaults(context.getVariables(scope), parameters);
        String testValue = parameters.getString("value");
        hasValue = testValue != null && !testValue.isEmpty();
        if (hasValue) {
            value = parameters.getDouble("value", 0);
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (!hasValue) {
            return SpellResult.NO_ACTION;
        }
        ConfigurationSection variables = context.getVariables(scope);
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
        context.getSpell().reloadParameters(context);
        return SpellResult.CAST;
    }
}
