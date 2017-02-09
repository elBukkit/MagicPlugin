package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public class BaseMagicProperties implements MagicProperties {

    private ConfigurationSection configuration = new MemoryConfiguration();
    private ConfigurationSection effectiveConfiguration = new MemoryConfiguration();
    private MagicProperties parent;
    private boolean dirty = false;
    
    @Override
    public void setProperty(String key, Object value) {
        configuration.set(key, value);
        dirty = true;
    }
    
    private void rebuildEffectiveConfiguration() {
        if (dirty) {
            effectiveConfiguration = ConfigurationUtils.cloneConfiguration(configuration);
            if (parent != null) {
                ConfigurationSection parentConfiguration = parent.getEffectiveConfiguration();
                ConfigurationUtils.addConfigurations(effectiveConfiguration, parentConfiguration);
            }
            dirty = false;
        }
    }

    public void load(ConfigurationSection configuration) {
        this.configuration = ConfigurationUtils.cloneConfiguration(configuration);
        dirty = true;
    }

    @Override
    public Object getProperty(String key) {
        rebuildEffectiveConfiguration();
        return effectiveConfiguration.get(key);
    }

    @Override
    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    @Override
    public ConfigurationSection getEffectiveConfiguration() {
        rebuildEffectiveConfiguration();
        return effectiveConfiguration;
    }

    @Override
    public void setParent(MagicProperties properties) {
        this.parent = properties;
        dirty = true;
    }
    
    public void clear() {
        configuration = new MemoryConfiguration();
        effectiveConfiguration = new MemoryConfiguration();
        parent = null;
        dirty = false;
    }
    
    public void configure(ConfigurationSection configuration) {
        ConfigurationUtils.addConfigurations(this.configuration, configuration);
    }

    public void upgrade(ConfigurationSection configuration) {
        // TODO: Safely? Need to special-case everything, or maybe only certain things?
        ConfigurationUtils.addConfigurations(this.configuration, configuration);
    }
}
