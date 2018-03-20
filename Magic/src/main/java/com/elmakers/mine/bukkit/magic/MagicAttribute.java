package com.elmakers.mine.bukkit.magic;

import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;

public class MagicAttribute {
    private final String key;
    private final Double defaultValue;
    private final Double min;
    private final Double max;

    public MagicAttribute(@Nonnull String key, @Nonnull ConfigurationSection configuration) {
        this.key = key;
        this.min = configuration.contains("min") ? configuration.getDouble("min") : null;
        this.max = configuration.contains("max") ? configuration.getDouble("max") : null;
        this.defaultValue = configuration.contains("default") ? configuration.getDouble("default") : null;
    }

    public @Nonnull  String getKey() {
        return key;
    }

    public Double getDefault() {
        return defaultValue;
    }

    public Double getMin() {
        return min;
    }

    public Double getMax() {
        return max;
    }
}
