package com.elmakers.mine.bukkit.magic;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;

public class SpellParameters extends ParameterizedConfiguration {
    private static Set<String> attributes;
    private final @Nonnull
    MageSpell spell;

    public SpellParameters(MageSpell spell) {
        super();
        this.spell = spell;
    }

    public SpellParameters(SpellParameters copy) {
        super();
        this.spell = copy.spell;
    }

    protected MageController getController() {
        return spell.getController();
    }

    protected Mage getMage() {
        return spell.getMage();
    }

    public static void initializeAttributes(Set<String> attrs) {
        attributes = new HashSet<>(attrs);
    }

    @Override
    protected double getParameter(String parameter) {
        Double value = spell.getAttribute(parameter);
        return value == null || Double.isNaN(value) || Double.isInfinite(value) ? 0 : value;
    }

    @Override
    protected Set<String> getParameters() {
        return attributes;
    }
}
