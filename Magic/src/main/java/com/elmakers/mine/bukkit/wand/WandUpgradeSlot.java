package com.elmakers.mine.bukkit.wand;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;

public class WandUpgradeSlot extends WandUpgradeSlotTemplate {
    private boolean hasDefaultSlotted = false;
    private Wand slotted;

    public WandUpgradeSlot(MagicController controller, String slotType) {
        super(controller, slotType);
    }

    public WandUpgradeSlot(MagicController controller, String configKey, ConfigurationSection config) {
        super(controller, configKey, config);
    }

    @Override
    protected void load(MagicController controller, ConfigurationSection config) {
        super.load(controller, config);
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
        if (slotType == null || slotType.isEmpty() || this.slotType == null) {
            return false;
        }
        if (!slotType.equals(this.slotType)) {
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
}
