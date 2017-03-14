package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;

public class WandTemplateProperties extends BaseMagicProperties{
    public WandTemplateProperties(MageController controller) {
        super(controller);
    }

    protected void clearProperty(String key) {
        configuration.set(key, null);
    }
}
