package com.elmakers.mine.bukkit.magic;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.MageSpell;

public class SpellParameters extends MageParameters {
    private @Nonnull ConfigurationSection castVariables;
    private @Nonnull ConfigurationSection spellVariables;
    private @Nonnull ConfigurationSection mageVariables;
    private final @Nonnull Set<String> allParameters = new HashSet<>();
    private final @Nonnull MageSpell spell;

    public SpellParameters(MageSpell spell) {
        super(spell.getMage(), "Spell: " + spell.getKey());
        this.spell = spell;
        this.spellVariables = spell.getVariables();
        this.mageVariables = spell.getMage() != null ? spell.getMage().getVariables() : null;
        Set<String> superParameters = super.getParameters();
        if (superParameters != null) {
            this.allParameters.addAll(superParameters);
        }
    }

    public SpellParameters(MageSpell spell, CastContext context) {
        this(spell);
        castVariables = context.getVariables();
    }

    public SpellParameters(MageSpell spell, ConfigurationSection config) {
        this(spell);
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
}
