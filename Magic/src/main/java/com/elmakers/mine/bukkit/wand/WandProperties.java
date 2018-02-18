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
        return hasOwnProperty(key) || (wandTemplate != null && wandTemplate.hasProperty(key))
            || (mageClass != null && mageClass.hasProperty(key));
    }

    @Override
    protected void migrateProperty(String key, MagicPropertyType propertyType) {
        super.migrateProperty(key, propertyType, wandTemplate);
    }

    @Override
    public Object getProperty(String key) {
        Object value = null;
        BaseMagicProperties storage = getStorage(key);
        if (storage != null) {
            value = storage.getProperty(key);
        }
        if (value == null) {
            value = super.getProperty(key);
        }
        if (value == null && wandTemplate != null) {
            value = wandTemplate.getProperty(key);
        }
        if (value == null && mageClass != null) {
            value = mageClass.getProperty(key);
        }
        // To preserve behavior of legacy wands, if a wand has no class assigned then it is not linked to Mage
        // data at all, so no need to check mage directly here.
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
