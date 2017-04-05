package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.CasterProperties;
import com.elmakers.mine.bukkit.magic.MageClass;
import com.elmakers.mine.bukkit.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.magic.BaseMagicConfigurable;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;

public class WandProperties extends CasterProperties {
    protected BaseMagicProperties wandTemplate;
    protected MageClass mageClass;

    public WandProperties(MageController controller) {
        super(MagicPropertyType.WAND, controller);
    }

    public void setWandTemplate(BaseMagicProperties properties) {
        this.wandTemplate = properties;
        dirty = true;
    }

    public void setMageClass(MageClass mageClass) {
        this.mageClass = mageClass;
        dirty = true;
    }

    public void clear() {
        super.clear();
        wandTemplate = null;
    }

    @Override
    protected void rebuildEffectiveConfiguration(ConfigurationSection effectiveConfiguration) {
        if (wandTemplate != null) {
            ConfigurationSection parentConfiguration = wandTemplate.getEffectiveConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, parentConfiguration);
        }
        if (mageClass != null) {
            ConfigurationSection classConfiguration = mageClass.getEffectiveConfiguration();
            ConfigurationUtils.overlayConfigurations(effectiveConfiguration, classConfiguration);
        }
    }
}
