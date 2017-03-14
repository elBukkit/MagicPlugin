package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public abstract class InheritedMagicProperties extends BaseMagicProperties {

    protected ConfigurationSection effectiveConfiguration = new MemoryConfiguration();
    protected boolean dirty = false;

    protected InheritedMagicProperties(MageController controller) {
        super(controller);
    }

    public ConfigurationSection getEffectiveConfiguration() {
        rebuildEffectiveConfiguration();
        return effectiveConfiguration;
    }
    
    protected void rebuildEffectiveConfiguration() {
        if (dirty) {
            effectiveConfiguration = ConfigurationUtils.cloneConfiguration(configuration);
            addEffectiveConfiguration(effectiveConfiguration);
            dirty = false;
        }
    }

    protected abstract void addEffectiveConfiguration(ConfigurationSection effectiveConfiguration);

    public void clear() {
        super.clear();
        effectiveConfiguration = new MemoryConfiguration();
    }
}
