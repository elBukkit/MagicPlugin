package com.elmakers.mine.bukkit.utility.platform;

import java.util.Collection;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public interface ItemUtils {
    Object getHandle(org.bukkit.inventory.ItemStack stack);

    Object getTag(Object mcItemStack);

    Object getTag(ItemStack itemStack);

    Object getOrCreateTag(Object mcItemStack);

    Object getOrCreateTag(ItemStack itemStack);

    ItemStack getCopy(ItemStack stack);

    ItemStack makeReal(ItemStack stack);

    void addGlow(ItemStack stack);

    void removeGlow(ItemStack stack);

    boolean isUnbreakable(ItemStack stack);

    void makeUnbreakable(ItemStack stack);

    void removeUnbreakable(ItemStack stack);

    void hideFlags(ItemStack stack, int flags);

    void makeTemporary(ItemStack itemStack, String message);

    boolean isTemporary(ItemStack itemStack);

    void makeUnplaceable(ItemStack itemStack);

    void removeUnplaceable(ItemStack itemStack);

    boolean isUnplaceable(ItemStack itemStack);

    String getTemporaryMessage(ItemStack itemStack);

    void setReplacement(ItemStack itemStack, ItemStack replacement);

    ItemStack getReplacement(ItemStack itemStack);

    boolean isEmpty(ItemStack itemStack);

    Object setStringList(Object nbtBase, String tag, Collection<String> values);

    List<String> getStringList(Object nbtBase, String tag);

    ItemStack getItem(Object itemTag);

    ItemStack[] getItems(Object rootTag, String tagName);

    boolean isSameItem(ItemStack first, ItemStack second);

    boolean hasSameTags(ItemStack first, ItemStack second);

    int getCustomModelData(ItemStack itemStack);

    void setCustomModelData(ItemStack itemStack, int customModelData);

    Object getEquippable(ItemStack itemStack);

    void setEquippable(ItemStack itemStack, Object equippable);
}
