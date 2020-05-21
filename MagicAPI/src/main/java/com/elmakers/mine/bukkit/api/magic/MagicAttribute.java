package com.elmakers.mine.bukkit.api.magic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

public class MagicAttribute {
    private final @Nonnull String key;
    private final @Nullable Double defaultValue;
    private final @Nullable Double min;
    private final @Nullable Double max;

    public MagicAttribute(@Nonnull String key, @Nonnull ConfigurationSection configuration) {
        this.key = key;
        this.min = configuration.contains("min") ? configuration.getDouble("min") : null;
        this.max = configuration.contains("max") ? configuration.getDouble("max") : null;
        this.defaultValue = configuration.contains("default") ? configuration.getDouble("default") : null;
    }

    public @Nonnull  String getKey() {
        return key;
    }

    public @Nullable Double getDefault() {
        return defaultValue;
    }

    public @Nullable Double getMin() {
        return min;
    }

    public @Nullable Double getMax() {
        return max;
    }

    public boolean inRange(double value) {
        if (min != null && value < min) return false;
        if (max != null && value > max) return false;
        return true;
    }
}
