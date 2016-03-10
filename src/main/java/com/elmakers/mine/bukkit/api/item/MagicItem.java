package com.elmakers.mine.bukkit.api.item;

import org.bukkit.inventory.ItemStack;

public class MagicItem {
    private String key;
    private ItemStack item;
    private double worth;
    
    public MagicItem(String key, ItemStack item, double worth) {
        this.key = key;
        this.item = item;
        this.worth = worth;
    }

    public String getKey() {
        return key;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getWorth() {
        return worth;
    }
}
