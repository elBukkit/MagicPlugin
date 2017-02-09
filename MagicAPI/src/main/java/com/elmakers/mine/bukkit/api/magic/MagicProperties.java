package com.elmakers.mine.bukkit.api.magic;

import org.bukkit.configuration.ConfigurationSection;

public interface MagicProperties {
    void setProperty(String key, Object value);
    Object getProperty(String key);

    ConfigurationSection getConfiguration();
    ConfigurationSection getEffectiveConfiguration();
    
    void setParent(MagicProperties properties);
    
    void load(ConfigurationSection configuration);
}
