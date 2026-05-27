package com.elmakers.mine.bukkit.requirements;

import org.bukkit.configuration.ConfigurationSection;

class DoubleRangedRequirement extends RangedRequirement<Double> {
    protected DoubleRangedRequirement() {
        super();
    }

    public DoubleRangedRequirement(String value) {
        super(value);
    }

    public DoubleRangedRequirement(ConfigurationSection configuration) {
        super(configuration);
    }

    @Override
    Double parseValue(String value) {
        return Double.parseDouble(value);
    }

    @Override
    public Double getValue(ConfigurationSection configuration, String key) {
        return configuration.getDouble(key);
    }
}
