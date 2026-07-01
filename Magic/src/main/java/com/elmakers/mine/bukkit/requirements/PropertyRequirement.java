package com.elmakers.mine.bukkit.requirements;

import org.bukkit.configuration.ConfigurationSection;

class PropertyRequirement extends DoubleRangedRequirement {
    public final String key;

    public PropertyRequirement(String type, ConfigurationSection configuration) {
        super(configuration);
        key = configuration.getString(type);
    }

    @Override
    public String toString() {
        return "[Require " + key + "=" + value + " from (" + min + " to " + max + ")]";
    }
}
