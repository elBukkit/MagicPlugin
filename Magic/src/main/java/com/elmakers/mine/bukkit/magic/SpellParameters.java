package com.elmakers.mine.bukkit.magic;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.VariableScope;
import com.elmakers.mine.bukkit.api.spell.MageSpell;

public class SpellParameters extends MageParameters {
    private @Nonnull ConfigurationSection castVariables;
    private @Nonnull ConfigurationSection spellVariables;
    private @Nonnull ConfigurationSection mageVariables;
    private final @Nonnull Set<String> allParameters = new HashSet<>();
    private final @Nonnull MageSpell spell;

    public SpellParameters(@Nonnull MageSpell spell, @Nullable ConfigurationSection mageVariables, @Nullable ConfigurationSection variables) {
        super(spell.getMage(), "Spell: " + spell.getKey());
        this.spell = spell;
        this.spellVariables = spell.getVariables();
        this.mageVariables = mageVariables;
        Set<String> superParameters = super.getParameters();
        if (superParameters != null) {
            this.allParameters.addAll(superParameters);
        }
        this.castVariables = variables;
    }

    public SpellParameters(@Nonnull MageSpell spell, @Nullable ConfigurationSection variables) {
        this(spell, spell.getMage() != null ? spell.getMage().getVariables() : null, variables);
    }

    public SpellParameters(@Nonnull MageSpell spell, @Nonnull CastContext context) {
        this(spell, context.getVariables());
    }

    public SpellParameters(@Nonnull MageSpell spell, @Nullable CastContext context, ConfigurationSection config) {
        this(spell, context);
        wrap(config);
    }

    public SpellParameters(SpellParameters copy) {
        super(copy);
        this.spell = copy.spell;
        this.castVariables = copy.castVariables;
        this.spellVariables = copy.spellVariables;
        this.mageVariables = copy.mageVariables;
        this.allParameters.addAll(copy.allParameters);
    }

    @Override
    protected double getParameter(String parameter) {
        if (castVariables != null && castVariables.contains(parameter)) {
            return castVariables.getDouble(parameter);
        }
        if (spellVariables != null && spellVariables.contains(parameter)) {
            return spellVariables.getDouble(parameter);
        }
        if (mageVariables != null && mageVariables.contains(parameter)) {
            return mageVariables.getDouble(parameter);
        }
        Double value = spell.getAttribute(parameter);
        return value == null || Double.isNaN(value) || Double.isInfinite(value) ? 0 : value;
    }

    @Override
    protected Set<String> getParameters() {
        if (castVariables != null) {
            this.allParameters.addAll(castVariables.getKeys(false));
        }
        if (spellVariables != null) {
            this.allParameters.addAll(spellVariables.getKeys(false));
        }
        if (mageVariables != null) {
            this.allParameters.addAll(mageVariables.getKeys(false));
        }
        return allParameters;
    }

    public void setMageVariables(@Nonnull ConfigurationSection variables) {
        this.mageVariables = variables;
    }

    public void setSpellVariables(@Nonnull ConfigurationSection variables) {
        this.spellVariables = variables;
    }

    @Nullable
    public ConfigurationSection getVariables(VariableScope scope) {
        switch (scope) {
            case CAST:
                return castVariables;
            case SPELL:
                return spellVariables;
            case MAGE:
                return mageVariables;
        }
        return null;
    }
}
