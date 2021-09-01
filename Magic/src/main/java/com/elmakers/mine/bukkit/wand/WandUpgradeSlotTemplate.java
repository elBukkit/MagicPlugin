package com.elmakers.mine.bukkit.wand;

import org.bukkit.configuration.ConfigurationSection;

public class WandUpgradeSlotTemplate {
    protected final String slotType;
    protected boolean hidden = false;
    protected boolean swappable = false;
    protected boolean replaceable = false;
    protected String defaultSlottedKey;

    public WandUpgradeSlotTemplate(String slotType) {
        this.slotType = slotType;
    }

    public WandUpgradeSlotTemplate(String configKey, ConfigurationSection config) {
        this(config.getString("type", configKey));
        load(config);
    }

    protected void load(ConfigurationSection config) {
        hidden = config.getBoolean("hidden", hidden);
        swappable = config.getBoolean("swappable", swappable);
        replaceable = config.getBoolean("replaceable", replaceable);
        defaultSlottedKey = config.getString("default_slotted", defaultSlottedKey);
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getType() {
        return slotType;
    }

    public String getDefaultSlottedKey() {
        return defaultSlottedKey;
    }
}
