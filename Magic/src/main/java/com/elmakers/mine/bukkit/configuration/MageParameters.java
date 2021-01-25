package com.elmakers.mine.bukkit.configuration;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class MageParameters extends ParameterizedConfiguration {
    private static Set<String> attributes;
    private final @Nullable
    Mage mage;

    public MageParameters(Mage mage, String context) {
        super(context);
        this.mage = mage;
    }

    public MageParameters(MageParameters copy) {
        super(copy);
        this.mage = copy.mage;
    }

    @Nullable
    protected MageController getController() {
        return mage == null ? null : mage.getController();
    }

    @Nullable
    protected Mage getMage() {
        return mage;
    }

    public static void initializeAttributes(Set<String> attrs) {
        attributes = new HashSet<>(attrs);
    }

    @Override
    @Nullable
    protected Double evaluate(String expression) {
        if (mage != null && mage.isPlayer()) {
            expression = mage.getController().setPlaceholders(mage.getPlayer(), expression);
        }
        return super.evaluate(expression);
    }

    @Override
    protected double getParameter(String parameter) {
        Double value = mage == null ? null : mage.getAttribute(parameter);
        return value == null || Double.isNaN(value) || Double.isInfinite(value) ? 0 : value;
    }

    @Override
    protected Set<String> getParameters() {
        return attributes;
    }
}
