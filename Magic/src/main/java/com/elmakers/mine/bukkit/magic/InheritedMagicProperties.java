package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class InheritedMagicProperties extends BaseMagicProperties {

    protected ConfigurationSection effectiveConfiguration = new MemoryConfiguration();
    protected boolean dirty = true;

    protected InheritedMagicProperties(@Nonnull MageController controller) {
        super(controller);
    }

    @Override
    public ConfigurationSection getEffectiveConfiguration() {
        rebuildEffectiveConfiguration();
        return effectiveConfiguration;
    }
    
    protected void rebuildEffectiveConfiguration() {
        if (dirty) {
            effectiveConfiguration = ConfigurationUtils.cloneConfiguration(configuration);
            rebuildEffectiveConfiguration(effectiveConfiguration);
            dirty = false;
        }
    }

    protected abstract void rebuildEffectiveConfiguration(@Nonnull ConfigurationSection effectiveConfiguration);

    @Override
    public void clear() {
        super.clear();
        effectiveConfiguration = new MemoryConfiguration();
        dirty = true;
    }

    @Override
    public void load(@Nullable ConfigurationSection configuration) {
        super.load(configuration);
        dirty = true;
    }
}
