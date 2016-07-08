package com.elmakers.mine.bukkit.api.block;

import org.bukkit.inventory.ItemStack;

public class CurrencyItem {
    private final ItemStack item;
    private final String name;
    private final String pluralName;
    private final double worth;

    public CurrencyItem(ItemStack item, double worth, String name, String pluralName) {
        this.item = item;
        this.name = name;
        this.worth = worth;
        this.pluralName = pluralName;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public String getPluralName() {
        return pluralName;
    }

    public double getWorth() {
        return worth;
    }
}
