package com.elmakers.mine.bukkit.utility.platform.v1_12;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.elmakers.mine.bukkit.utility.platform.Platform;

public class CompatibilityUtils extends com.elmakers.mine.bukkit.utility.platform.legacy.CompatibilityUtils {

    public CompatibilityUtils(Platform platform) {
        super(platform);
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, final String name) {
        size = (int) (Math.ceil((double) size / 9) * 9);
        size = Math.min(size, 54);
        String translatedName = translateColors(name);
        return Bukkit.createInventory(holder, size, translatedName);
    }
}
