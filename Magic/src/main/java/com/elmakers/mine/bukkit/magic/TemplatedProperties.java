package com.elmakers.mine.bukkit.magic;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class TemplatedProperties extends CasterProperties {
    public TemplatedProperties(MagicPropertyType type, MageController controller) {
        super(type, controller);
    }

    @Nullable
    @Override
    public ConfigurationSection getConfigurationSection(String key) {
        ConfigurationSection own = super.getConfigurationSection(key);
        BaseMagicProperties template = getTemplate();
        ConfigurationSection fromTemplate = template == null ? null : template.getConfigurationSection(key);

        if (own == null) {
            return fromTemplate;
        }
        if (fromTemplate != null) {
            own = ConfigurationUtils.cloneConfiguration(own);
            own = ConfigurationUtils.overlayConfigurations(own, fromTemplate);
        }
        return own;
    }

    @Nullable
    protected abstract BaseMagicProperties getTemplate();
}
