package com.elmakers.mine.bukkit.wand;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;

public class WandUpgradeSlot {
    private final String slotType;
    private boolean hidden = false;
    private boolean swappable = false;
    private boolean replaceable = false;
    private Wand slotted;

    public WandUpgradeSlot(String slotType) {
        this.slotType = slotType;
    }

    public WandUpgradeSlot(String configKey, ConfigurationSection config) {
        slotType = config.getString("type", configKey);
        hidden = config.getBoolean("hidden", false);
        swappable = config.getBoolean("swappable", false);
        replaceable = config.getBoolean("replaceable", false);
    }

    public Wand getSlotted() {
        return slotted;
    }

    public boolean addSlotted(Wand upgrade, Mage mage) {
        String slotType = upgrade.getSlot();
        if (slotType == null || slotType.isEmpty()) {
            return false;
        }
        if (!slotType.equals(slotType)) {
            return false;
        }
        if (slotted == null || replaceable) {
            slotted = upgrade;
            return true;
        }
        if (!swappable || mage == null) {
            return false;
        }
        mage.giveItem(slotted.getItem());
        slotted = upgrade;
        return true;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getType() {
        return slotType;
    }
}
