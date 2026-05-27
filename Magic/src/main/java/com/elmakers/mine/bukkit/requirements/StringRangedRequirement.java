package com.elmakers.mine.bukkit.requirements;

import org.bukkit.configuration.ConfigurationSection;

class StringRangedRequirement extends RangedRequirement<String> {
    public StringRangedRequirement(String value) {
        super(value);
    }

    public StringRangedRequirement(ConfigurationSection configuration) {
        super(configuration);
    }

    @Override
    String parseValue(String value) {
        return value;
    }

    @Override
    public String getValue(ConfigurationSection configuration, String key) {
        return configuration.getString(key);
    }
}
