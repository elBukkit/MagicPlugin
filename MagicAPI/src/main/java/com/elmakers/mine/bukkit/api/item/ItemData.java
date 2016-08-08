package com.elmakers.mine.bukkit.api.item;

import java.util.Set;

import org.bukkit.inventory.ItemStack;

public interface ItemData {
    public String getKey();
    public double getWorth();
    public ItemStack getItemStack(int amount);
    public String getCreator();
    public String getCreatorId();
    Set<String> getCategories();
}
