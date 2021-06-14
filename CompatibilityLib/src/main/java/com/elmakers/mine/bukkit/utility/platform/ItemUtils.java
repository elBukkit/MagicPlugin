package com.elmakers.mine.bukkit.utility.platform;

import java.util.Collection;

import org.bukkit.inventory.ItemStack;

public interface ItemUtils {
    Object getHandle(org.bukkit.inventory.ItemStack stack);

    Object getTag(Object mcItemStack);

    Object getTag(ItemStack itemStack);

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

    ItemStack getItem(Object itemTag);

    ItemStack[] getItems(Object rootTag, String tagName);
}
