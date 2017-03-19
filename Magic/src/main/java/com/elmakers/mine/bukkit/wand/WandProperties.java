package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicPropertyType;
import com.elmakers.mine.bukkit.magic.BaseMagicConfigurable;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;

public class WandProperties extends BaseMagicConfigurable {
    protected BaseMagicProperties wandTemplate;

    public WandProperties(MageController controller) {
        super(MagicPropertyType.WAND, controller);
    }

    public void setWandTemplate(BaseMagicProperties properties) {
        this.wandTemplate = properties;
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
            ConfigurationUtils.addConfigurations(effectiveConfiguration, parentConfiguration, false);
        }
    }
}
