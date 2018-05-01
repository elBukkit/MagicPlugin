package com.elmakers.mine.bukkit.economy;

import org.bukkit.configuration.ConfigurationSection;

public class Currency {
    private final double defaultValue;
    private final double maxValue;

    public Currency(ConfigurationSection configuration) {
        defaultValue = configuration.getDouble("default", 0);
        maxValue = configuration.getDouble("max", -1);
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public boolean hasMaxValue() {
        return maxValue >= 0;
    }
}
