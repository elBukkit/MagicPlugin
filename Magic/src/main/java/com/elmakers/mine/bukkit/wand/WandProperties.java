package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.CasterProperties;
import com.elmakers.mine.bukkit.magic.MageClass;
import com.elmakers.mine.bukkit.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;

public abstract class WandProperties extends CasterProperties {
    protected BaseMagicProperties wandTemplate;
    protected MageClass mageClass;

    public WandProperties(MageController controller) {
        super(MagicPropertyType.WAND, controller);
    }

    public void setWandTemplate(BaseMagicProperties properties) {
        this.wandTemplate = properties;
    }

    public void setMageClass(MageClass mageClass) {
        this.mageClass = mageClass;
    }

    @Override
    public void clear() {
        super.clear();
        wandTemplate = null;
    }

    @Override
    public boolean hasProperty(String key) {
        BaseMagicProperties storage = getStorage(key);
        if (storage != null) {
            return storage.hasOwnProperty(key);
        }
        return hasOwnProperty(key) || (wandTemplate != null && wandTemplate.hasProperty(key));
    }

    @Override
    protected void migrateProperty(String key, MagicPropertyType propertyType) {
        super.migrateProperty(key, propertyType, wandTemplate);
    }

    @Override
    public Object getInheritedProperty(String key) {
        Object value = super.getProperty(key);
        if (value == null && wandTemplate != null) {
            value = wandTemplate.getProperty(key);
        }
        if (value == null && mageClass != null) {
            value = mageClass.getInheritedProperty(key);
        }
        return value;
    }

    @Override
    public Object getProperty(String key) {
        Object value = null;
        BaseMagicProperties storage = getStorage(key);
        if (storage != null && storage != this) {
            value = storage.getProperty(key);
        }
        if (value == null) {
            value = super.getProperty(key);
        }
        if (value == null && wandTemplate != null) {
            value = wandTemplate.getProperty(key);
        }
        return value;
    }

    public ConfigurationSection getEffectiveConfiguration() {
        ConfigurationSection effectiveConfiguration = ConfigurationUtils.cloneConfiguration(getConfiguration());
        if (wandTemplate != null) {
            ConfigurationSection parentConfiguration = wandTemplate.getConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, parentConfiguration);
        }
        if (mageClass != null) {
            ConfigurationSection classConfiguration = mageClass.getConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, classConfiguration);
        }
        return effectiveConfiguration;
    }
}
