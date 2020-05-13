package com.elmakers.mine.bukkit.magic;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.MageSpell;

public class SpellParameters extends MageParameters {
    private @Nonnull ConfigurationSection variables;
    private final @Nonnull Set<String> allParameters = new HashSet<>();
    private final @Nonnull MageSpell spell;

    public SpellParameters(MageSpell spell) {
        super(spell.getMage(), "Spell: " + spell.getKey());
        this.spell = spell;
        this.variables = spell.getVariables();
        this.allParameters.addAll(super.getParameters());
    }

    public SpellParameters(MageSpell spell, ConfigurationSection config) {
        this(spell);
        wrap(config);
    }

    public SpellParameters(SpellParameters copy) {
        super(copy);
        this.spell = copy.spell;
        this.variables = copy.variables;
        this.allParameters.addAll(copy.allParameters);
    }

    @Override
    protected double getParameter(String parameter) {
        if (variables.contains(parameter)) {
            return variables.getDouble(parameter);
        }
        Double value = spell.getAttribute(parameter);
        return value == null || Double.isNaN(value) || Double.isInfinite(value) ? 0 : value;
    }

    @Override
    protected Set<String> getParameters() {
        this.allParameters.addAll(variables.getKeys(false));
        return allParameters;
    }

    public void setVariables(@Nonnull ConfigurationSection variables) {
        this.variables = variables;
    }
}
