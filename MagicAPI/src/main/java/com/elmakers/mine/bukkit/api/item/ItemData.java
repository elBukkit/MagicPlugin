package com.elmakers.mine.bukkit.api.item;

import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;

public interface ItemData {
    String getKey();
    String getBaseKey();
    double getWorth();
    double getEarns();
    boolean hasCustomEarns();
    @Nullable
    ItemStack getItemStack(int amount, ItemUpdatedCallback callback);
    @Nullable
    ItemStack getItemStack(int amount);
    @Nullable
    ItemStack getItemStack();
    String getCreator();
    String getCreatorId();
    Set<String> getCategories();
    Material getType();
    @Nullable
    ItemMeta getItemMeta();
    @Nullable
    MaterialAndData getMaterialAndData();
    boolean isLocked();
    int getDurability();
    int getAmount();

    @Nullable
    @Deprecated
    org.bukkit.material.MaterialData getMaterialData();
}
