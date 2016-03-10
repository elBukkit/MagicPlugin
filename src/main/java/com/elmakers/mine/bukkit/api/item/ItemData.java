package com.elmakers.mine.bukkit.api.item;

import org.bukkit.inventory.ItemStack;

public interface ItemData {
    public String getKey();
    public double getWorth();
    public ItemStack getItemStack(int amount);
}
