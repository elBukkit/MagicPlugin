package com.elmakers.mine.bukkit.wand;

import org.bukkit.inventory.ItemStack;

public class WandInventory {
    public final ItemStack[] items;

    public WandInventory(int size) {
        items = new ItemStack[size];
    }

    public int getSize() {
        return items.length;
    }

    public ItemStack getItem(int index) {
        if (index < 0 || index >= items.length) throw new IllegalArgumentException("WandInventory index out of range: " + index + " not between 0 and " + (items.length - 1));
        return items[index];
    }

    public void setItem(int index, ItemStack item) {
        if (index < 0 || index >= items.length) throw new IllegalArgumentException("WandInventory index out of range: " + index + " not between 0 and " + (items.length - 1));
        items[index] = item;
    }

    public boolean addItem(ItemStack item) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                items[i] = item;
                return true;
            }
        }

        return false;
    }

    public void clear() {
        for (int i = 0; i < items.length; i++) {
            items[i] = null;
        }
    }

    public ItemStack[] getContents() {
        return items;
    }
}
