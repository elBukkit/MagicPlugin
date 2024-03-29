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
import com.elmakers.mine.bukkit.configuration.SpellParameters;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ModifyVariableAction extends BaseSpellAction {
    private String key;
    private boolean clear;
    private VariableScope scope = VariableScope.CAST;
    private ConfigurationSection parameters;

    private void checkDefaults(ConfigurationSection variables, ConfigurationSection parameters) {
        if (!clear && !variables.contains(key)) {
            double defaultValue = parameters.getDouble("default", 0);
            variables.set(key, defaultValue);
        } else if (clear && variables.contains(key)) {
            variables.set(key, null);
        }
    }

    private void parseScope(ConfigurationSection parameters, VariableScope defaultScope, Logger logger) {
        defaultScope = defaultScope == null ? scope : defaultScope;
        scope = ConfigurationUtils.parseScope(parameters.getString("scope"), defaultScope, logger);
    }

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters) {
        key = parameters.getString("variable", "");
        clear = parameters.getBoolean("clear");
        parseScope(parameters, spell.getVariableScope(key), spell.getController().getLogger());
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
        key = parameters.getString("variable", "");
        parseScope(parameters, context.getSpell().getVariableScope(key), context.getLogger());
        clear = parameters.getBoolean("clear");
        checkDefaults(context.getVariables(scope), parameters);
        this.parameters = parameters;
    }

    @Override
    public SpellResult perform(CastContext context) {
        // Don't fetch the value at prepare() time in case another cast has modified it
        String testValue = parameters.getString("value");
        boolean hasValue = testValue != null && !testValue.isEmpty();
        if (!hasValue) {
            return SpellResult.NO_ACTION;
        }

        // This is kind of ugly, but it forces context-specific variables (like target location)
        // to read from the current context state.
        SpellParameters spellParameters = parameters instanceof SpellParameters ? (SpellParameters)parameters : null;
        CastContext originalContext = spellParameters != null ? spellParameters.getContext() : null;
        if (spellParameters != null) {
            spellParameters.setContext(context);
        }
        double value = parameters.getDouble("value", 0);
        if (originalContext != null) {
            spellParameters.setContext(originalContext);
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
