package com.elmakers.mine.bukkit.wand;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class WandUpgradeSlot extends WandUpgradeSlotTemplate {
    private boolean hasDefaultSlotted = false;
    private Wand slotted;

    public WandUpgradeSlot(MagicController controller, String slotType) {
        this(controller, slotType, null);
    }

    public WandUpgradeSlot(MagicController controller, String slotType, ConfigurationSection config) {
        super(slotType);
        WandUpgradeSlotTemplate template = controller.getWandSlotTemplate(slotType);
        if (template != null) {
            this.setTemplate(template);
        }
        if (config != null) {
            load(controller, config);
        }
    }

    protected void setTemplate(WandUpgradeSlotTemplate template) {
        hidden = template.hidden;
        swappable = template.swappable;
        replaceable = template.replaceable;
        defaultSlottedKey = template.defaultSlottedKey;
    }

    protected void load(MagicController controller, ConfigurationSection config) {
        super.load(config);
        if (defaultSlottedKey != null && !defaultSlottedKey.isEmpty() && slotted == null) {
            slotted = controller.createWand(defaultSlottedKey);
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

    public void addInstructionLore(List<String> lore, Messages messages) {
        String instructions = messages.getIfSet("wand_slots." + slotType + ".lore_instructions");
        if (slotted != null) {
            instructions = slotted.getMessage("slotted_lore_instructions", instructions);
        }
        CompatibilityLib.getInventoryUtils().wrapText(instructions, lore);
    }

    public void showControlInstructions(Mage mage, Messages messages) {
        String instructions = messages.getIfSet("wand_slots." + slotType + ".control_instructions");
        if (slotted != null) {
            instructions = slotted.getMessage("slotted_control_instructions", instructions);
        }
        mage.sendMessage(instructions);
    }
}
