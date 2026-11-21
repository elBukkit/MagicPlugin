package com.elmakers.mine.bukkit.utility.platform;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public interface MiniMessageUtils {
    String toRichText(String text);

    String toLegacyText(String text);

    Inventory createInventory(InventoryHolder holder, int size, String text);

    Inventory createInventory(InventoryHolder holder, InventoryType type, String text);
}
