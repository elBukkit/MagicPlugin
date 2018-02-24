package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

import java.util.Set;

public class AttributableConfiguration extends ParameterizedConfiguration {
    private static final String STORE_KEY = "attributes";
    private Mage mage;
    
    public AttributableConfiguration() {
        super(STORE_KEY);
    }

    public AttributableConfiguration(Mage mage) {
        super(STORE_KEY);
        this.mage = mage;
    }

    public AttributableConfiguration(AttributableConfiguration copy) {
        super(STORE_KEY);
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
        initializeParameters(STORE_KEY, attrs);
    }

    protected double getParameter(String parameter) {
        Double value = mage == null ? null : mage.getAttribute(parameter);
        return value == null ? 0 : value;
    }
}
