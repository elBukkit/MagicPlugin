package com.elmakers.mine.bukkit.item;

import org.bukkit.inventory.Inventory;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public enum InventorySlot {
    HELMET(39), CHESTPLATE(38), LEGGINGS(37), BOOTS(36),

    MAIN_HAND(), OFF_HAND(), FREE(),

    // This is here for armor stands
    RIGHT_ARM();

    private final int slot;

    InventorySlot() {
        this(-1);
    }

    InventorySlot(int slot) {
        this.slot = slot;
    }

    public static InventorySlot getArmorSlot(int slot) {
        slot = Math.max(Math.min(slot, 3), 0);
        switch (slot) {
            case 0: return BOOTS;
            case 1: return LEGGINGS;
            case 2: return CHESTPLATE;
            case 3: return HELMET;
        }
        return HELMET;
    }

    public int getArmorSlot() {
        return slot - 36;
    }

    public boolean isArmorSlot() {
        return slot != -1;
    }

    public int getSlot() {
        return slot;
    }

    public int getSlot(Mage mage) {
        if (slot != -1 || !mage.isPlayer()) {
            return slot;
        }
        switch (this) {
            case MAIN_HAND:
                return mage.getPlayer().getInventory().getHeldItemSlot();
            case OFF_HAND:
                return 40;
            case FREE:
                Inventory inventory = mage.getInventory();
                for (int i = 0; i < inventory.getSize(); i++) {
                    if (CompatibilityUtils.isEmpty(inventory.getItem(i))) {
                        return i;
                    }
                }
            break;
        }
        return -1;
    }
}
