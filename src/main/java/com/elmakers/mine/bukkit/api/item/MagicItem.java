package com.elmakers.mine.bukkit.api.item;

import org.bukkit.inventory.ItemStack;

public interface MagicItem {
    public String getKey();
    public double getWorth();
    public ItemStack getItem(int amount);
}
