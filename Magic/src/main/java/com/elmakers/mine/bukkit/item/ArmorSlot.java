package com.elmakers.mine.bukkit.item;

public enum ArmorSlot {
    HELMET(39), CHESTPLATE(38), LEGGINGS(37), BOOTS(36),

    // This is here for armor stands
    RIGHT_ARM(-1);

    private final int slot;

    ArmorSlot(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
