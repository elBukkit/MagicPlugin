package com.elmakers.mine.bukkit.requirements;

import org.bukkit.configuration.ConfigurationSection;

abstract class RangedRequirement<T extends Comparable<T>> {
    public T max;
    public T min;
    public T value;
    public boolean inclusive = false;

    protected RangedRequirement() {
    }

    public RangedRequirement(String value) {
        try {
            if (value.startsWith("<")) {
                if (value.startsWith("<=")) {
                    max = parseValue(value.substring(2));
                    inclusive = true;
                } else {
                    max = parseValue(value.substring(1));
                }
            } else if (value.startsWith(">")) {
                if (value.startsWith(">=")) {
                    min = parseValue(value.substring(2));
                    inclusive = true;
                } else {
                    min = parseValue(value.substring(1));
                }
            } else if (value.startsWith("=")) {
                this.value = parseValue(value.substring(1));
            } else {
                // Default to >= which is what we normally mean
                this.min = parseValue(value);
                this.inclusive = true;
            }
        } catch (Exception ignore) {
        }
    }

    public RangedRequirement(ConfigurationSection configuration) {
        if (configuration.contains("min")) {
            min = getValue(configuration, "min");
        }
        if (configuration.contains("max")) {
            max = getValue(configuration, "max");
        }
        if (configuration.contains("value")) {
            value = getValue(configuration, "value");
        }
        inclusive = configuration.getBoolean("inclusive");
    }

    abstract T parseValue(String value);
    abstract T getValue(ConfigurationSection configuration, String key);

    public boolean check(T value) {
        if (this.value != null && (value == null || !value.equals(this.value))) return false;
        if (inclusive)  {
            if (this.min != null && (value == null || value.compareTo(this.min) < 0)) return false;
            if (this.max != null && (value != null && value.compareTo(this.max) > 0)) return false;
        } else {
            if (this.min != null && (value == null || value.compareTo(this.min) <= 0)) return false;
            if (this.max != null && (value != null && value.compareTo(this.max) >= 0)) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[Require =" + value + " from (" + min + " to " + max + ")]";
    }
}
