package com.elmakers.mine.bukkit.api.item;

import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

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
    MaterialData getMaterialData();
    @Nullable
    ItemMeta getItemMeta();
}
