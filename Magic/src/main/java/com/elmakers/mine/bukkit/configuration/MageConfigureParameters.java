package com.elmakers.mine.bukkit.configuration;

import java.util.Set;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicConfigurable;

public class MageConfigureParameters extends MageParameters {
    private static final String PROPERTY_VARIABLE = "x";
    protected final double propertyValue;

    public MageConfigureParameters(Mage mage, String context, MagicConfigurable target, String property) {
        super(mage, context);
        propertyValue = target.getProperty(property, Double.NaN);
        silent = true;
        defaultValue = null;
    }

    @Override
    protected double getParameter(String parameter) {
        if (PROPERTY_VARIABLE.equals(parameter)) {
            return propertyValue;
        }
        return super.getParameter(parameter);
    }

    @Override
    protected Set<String> getParameters() {
        Set<String> parameters = super.getParameters();
        parameters.add(PROPERTY_VARIABLE);
        return parameters;
    }
}
