package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

import java.util.Set;

public class AttributableConfiguration extends ParameterizedConfiguration {
    private static Set<String> attributes;
    private Mage mage;
    
    public AttributableConfiguration() {
        super();
    }

    public AttributableConfiguration(Mage mage) {
        super();
        this.mage = mage;
    }

    public AttributableConfiguration(AttributableConfiguration copy) {
        super();
        this.mage = copy.mage;
    }

    protected MageController getController() {
        return mage == null ? null : mage.getController();
    }

    protected Mage getMage() {
        return mage;
    }
    
    public void setMage(Mage mage) {
        this.mage = mage;
    }

    public static void initializeAttributes(Set<String> attrs) {
        attributes = attrs;
    }

    @Override
    protected double getParameter(String parameter) {
        Double value = mage == null ? null : mage.getAttribute(parameter);
        return value == null ? 0 : value;
    }

    @Override
    protected Set<String> getParameters() {
        return attributes;
    }
}
