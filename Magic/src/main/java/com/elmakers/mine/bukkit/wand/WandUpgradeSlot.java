package com.elmakers.mine.bukkit.wand;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;

public class WandUpgradeSlot {
    private final String slotType;
    private boolean hidden = false;
    private boolean swappable = false;
    private boolean replaceable = false;
    private boolean hasDefaultSlotted = false;
    private Wand slotted;

    public WandUpgradeSlot(MagicController controller, String slotType) {
        this.slotType = slotType;
        ConfigurationSection defaultConfig = controller.getWandSlotConfiguration(slotType);
        if (defaultConfig != null) {
            load(controller, defaultConfig);
        }
    }

    public WandUpgradeSlot(MagicController controller, String configKey, ConfigurationSection config) {
        this(controller, config.getString("type", configKey));
        load(controller, config);
    }

    private void load(MagicController controller, ConfigurationSection config) {
        hidden = config.getBoolean("hidden", hidden);
        swappable = config.getBoolean("swappable", swappable);
        replaceable = config.getBoolean("replaceable", replaceable);
        String defaultKey = config.getString("default_slotted");
        if (defaultKey != null && !defaultKey.isEmpty() && slotted == null) {
            slotted = controller.createWand(defaultKey);
            hasDefaultSlotted = true;
        }
    }

    public boolean hasDefaultSlotted() {
        return hasDefaultSlotted;
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
            hasDefaultSlotted = false;
            return true;
        }
        if (!swappable || mage == null) {
            return false;
        }
        mage.giveItem(slotted.getItem());
        slotted = upgrade;
        hasDefaultSlotted = false;
        return true;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getType() {
        return slotType;
    }
}
