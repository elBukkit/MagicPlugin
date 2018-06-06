package com.elmakers.mine.bukkit.magic;

import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.MageSpell;

public class SpellParameters extends MageParameters {
    private final @Nonnull
    MageSpell spell;

    public SpellParameters(MageSpell spell) {
        super(spell.getMage());
        this.spell = spell;
    }

    public SpellParameters(MageSpell spell, ConfigurationSection config) {
        super(spell.getMage());
        this.spell = spell;
        wrap(config);
    }

    public SpellParameters(SpellParameters copy) {
        super(copy);
        this.spell = copy.spell;
    }

    @Override
    protected double getParameter(String parameter) {
        Double value = spell.getAttribute(parameter);
        return value == null || Double.isNaN(value) || Double.isInfinite(value) ? 0 : value;
    }
}
