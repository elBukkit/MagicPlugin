package com.elmakers.mine.bukkit.magic;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class MageParameters extends ParameterizedConfiguration {
    private static Set<String> attributes;
    private final @Nonnull
    Mage mage;

    public MageParameters(Mage mage) {
        super();
        this.mage = mage;
    }

    public MageParameters(MageParameters copy) {
        super();
        this.mage = copy.mage;
    }

    protected MageController getController() {
        return mage.getController();
    }

    protected Mage getMage() {
        return mage;
    }

    public static void initializeAttributes(Set<String> attrs) {
        attributes = new HashSet<>(attrs);
    }

    @Override
    protected double getParameter(String parameter) {
        Double value = mage.getAttribute(parameter);
        return value == null || Double.isNaN(value) || Double.isInfinite(value) ? 0 : value;
    }

    @Override
    protected Set<String> getParameters() {
        return attributes;
    }
}
