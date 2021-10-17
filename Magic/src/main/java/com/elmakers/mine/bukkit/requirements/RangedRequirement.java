package com.elmakers.mine.bukkit.requirements;

import org.bukkit.configuration.ConfigurationSection;

class RangedRequirement {
    public Double max;
    public Double min;
    public Double value;
    public boolean inclusive = false;

    protected RangedRequirement() {
    }

    public RangedRequirement(String value) {
        try {
            if (value.startsWith("<")) {
                if (value.startsWith("<=")) {
                    max = Double.parseDouble(value.substring(2));
                    inclusive = true;
                } else {
                    max = Double.parseDouble(value.substring(1));
                }
            } else if (value.startsWith(">")) {
                if (value.startsWith(">=")) {
                    min = Double.parseDouble(value.substring(2));
                    inclusive = true;
                } else {
                    min = Double.parseDouble(value.substring(1));
                }
            } else if (value.startsWith("=")) {
                this.value = Double.parseDouble(value.substring(1));
            } else {
                // Default to >= which is what we normally mean
                this.min = Double.parseDouble(value);
                this.inclusive = true;
            }
        } catch (Exception ignore) {
        }
    }

    public RangedRequirement(ConfigurationSection configuration) {
        if (configuration.contains("min")) {
            min = configuration.getDouble("min");
        }
        if (configuration.contains("max")) {
            max = configuration.getDouble("max");
        }
        if (configuration.contains("value")) {
            value = configuration.getDouble("value");
        }
        inclusive = configuration.getBoolean("inclusive");
    }

    public boolean check(Double value) {
        if (this.value != null && (value == null || !value.equals(this.value))) return false;
        if (inclusive)  {
            if (this.min != null && (value == null || value < this.min)) return false;
            if (this.max != null && (value != null && value > this.max)) return false;
        } else {
            if (this.min != null && (value == null || value <= this.min)) return false;
            if (this.max != null && (value != null && value >= this.max)) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[Require =" + value + " from (" + min + " to " + max + ")]";
    }
}
