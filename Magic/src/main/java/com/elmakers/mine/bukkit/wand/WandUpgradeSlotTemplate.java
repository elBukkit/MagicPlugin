package com.elmakers.mine.bukkit.wand;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.magic.MagicController;

public class WandUpgradeSlotTemplate {
    protected final String slotType;
    protected boolean hidden = false;
    protected boolean swappable = false;
    protected boolean replaceable = false;

    public WandUpgradeSlotTemplate(MagicController controller, String slotType) {
        this.slotType = slotType;
        ConfigurationSection defaultConfig = controller.getWandSlotConfiguration(slotType);
        if (defaultConfig != null) {
            load(controller, defaultConfig);
        }
    }

    public WandUpgradeSlotTemplate(MagicController controller, String configKey, ConfigurationSection config) {
        this(controller, config.getString("type", configKey));
        load(controller, config);
    }

    protected void load(MagicController controller, ConfigurationSection config) {
        hidden = config.getBoolean("hidden", hidden);
        swappable = config.getBoolean("swappable", swappable);
        replaceable = config.getBoolean("replaceable", replaceable);
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getType() {
        return slotType;
    }
}
