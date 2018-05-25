package com.elmakers.mine.bukkit.api.item;

import javax.annotation.Nullable;

import org.bukkit.inventory.ItemStack;

/**
 * This callback is generally only called once, after an item has finalized.
 * This only really applies to player skull items, which may need to fetch player skins asynchronously.
 * In all cases, the callback is guaranteed to be called, though it may be called immediately. It is always
 * called on the main thread.
 * The ItemStack instance returns in the updated callback may be different that any instance returned immediately
 * by the method called.
 */
public interface ItemUpdatedCallback {
    void updated(@Nullable ItemStack itemStack);
}
