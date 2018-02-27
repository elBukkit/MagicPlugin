package com.elmakers.mine.bukkit.api.requirements;

import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;

public class Requirement {
    public final static String DEFAULT_TYPE = "magic";
    private final String type;
    private final ConfigurationSection configuration;
    
    public Requirement(ConfigurationSection configuration) {
        this.configuration = configuration;
        this.type = configuration.getString("type", DEFAULT_TYPE);
    }
    
    public @Nonnull String getType() {
        return type;
    }
    
    public @Nonnull ConfigurationSection getConfiguration() {
        return configuration;
    }
}
