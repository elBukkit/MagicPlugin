package com.elmakers.mine.bukkit.economy;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class Currency {
    private final double defaultValue;
    private final double maxValue;
    private final MaterialAndData icon;

    public Currency(ConfigurationSection configuration) {
        defaultValue = configuration.getDouble("default", 0);
        maxValue = configuration.getDouble("max", -1);
        icon = ConfigurationUtils.getMaterialAndData(configuration, "icon");
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

    public MaterialAndData getIcon() {
        return icon;
    }
}
