package com.elmakers.mine.bukkit.api.item;

import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface ItemData {
    String getKey();
    double getWorth();
    @Nullable
    ItemStack getItemStack(int amount);
    String getCreator();
    String getCreatorId();
    Set<String> getCategories();
    Material getType();
    @Nullable
    ItemMeta getItemMeta();
    short getDurability();
}
