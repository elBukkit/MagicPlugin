package com.elmakers.mine.bukkit.wand;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;
import com.elmakers.mine.bukkit.magic.MageClass;
import com.elmakers.mine.bukkit.magic.TemplatedProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public abstract class WandProperties extends TemplatedProperties {
    protected BaseMagicProperties wandTemplate;
    protected MageClass mageClass;

    public WandProperties(MageController controller) {
        super(MagicPropertyType.WAND, controller);
    }

    public void setWandTemplate(WandTemplate template) {
        Mage mage = getMage();
        if (mage != null) {
            template = template.getMageTemplate(mage);
        }
        this.wandTemplate = template;
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
    public boolean hasOwnProperty(String key) {
        return super.hasOwnProperty(key) || (wandTemplate != null && wandTemplate.hasOwnProperty(key));
    }

    @Override
    protected void migrateProperty(String key, MagicPropertyType propertyType) {
        super.migrateProperty(key, propertyType, wandTemplate);
    }

    @Override
    @Nullable
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
    @Nullable
    public ConfigurationSection getPropertyConfiguration(String key) {
        BaseMagicProperties storage = getStorage(key);
        if (storage != null && storage != this) {
            return storage.getPropertyConfiguration(key);
        }
        if (configuration.contains(key)) {
            return configuration;
        }
        return wandTemplate == null ? configuration : wandTemplate.getConfiguration();
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
