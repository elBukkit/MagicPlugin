package com.elmakers.mine.bukkit.arena;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ArenaTemplate {
    private final String key;
    private final ConfigurationSection configuration;

    public ArenaTemplate(String key, ConfigurationSection configuration) {
        this.key = key;
        this.configuration = configuration;
    }

    public ArenaTemplate(String key) {
        this(key, ConfigurationUtils.newConfigurationSection());
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    public String getKey() {
        return key;
    }
}
