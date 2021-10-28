package com.elmakers.mine.bukkit.item;

public enum ArmorSlot {
    HELMET(39), CHESTPLATE(38), LEGGINGS(37), BOOTS(36),

    MAIN_HAND(), OFF_HAND(), FREE(),

    // This is here for armor stands
    RIGHT_ARM(),

    // This is here for non armor-slots
    INVENTORY();

    private final int slot;

    ArmorSlot() {
        this(-1);
    }

    ArmorSlot(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public boolean isArmorSlot() {
        return slot >= 36 && slot <= 39;
    }
}
