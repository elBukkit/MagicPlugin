package com.elmakers.mine.bukkit.api.item;

import java.util.Set;

import org.bukkit.inventory.ItemStack;

public interface ItemData {
    String getKey();
    double getWorth();
    ItemStack getItemStack(int amount);
    String getCreator();
    String getCreatorId();
    Set<String> getCategories();
}
