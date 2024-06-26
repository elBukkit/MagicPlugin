package com.elmakers.mine.bukkit.utility.platform;

import org.bukkit.inventory.ItemStack;

public interface ItemUtils {
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

    boolean isSameItem(ItemStack first, ItemStack second);
}
