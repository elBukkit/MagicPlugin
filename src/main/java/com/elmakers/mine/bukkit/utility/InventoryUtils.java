package com.elmakers.mine.bukkit.utility;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils extends NMSUtils
{	
    public static boolean saveTagsToNBT(ConfigurationSection tags, Object node, String[] tagNames)
    {
        if (node == null) {
            Bukkit.getLogger().warning("Tring to save tags to a null node");
            return false;
        }
        if (!class_NBTTagCompound.isAssignableFrom(node.getClass())) {
            Bukkit.getLogger().warning("Tring to save tags to a non-CompoundTag");
            return false;
        }
        for (String tagName : tagNames)
        {
            String value = tags.getString(tagName);
            // This is kinda hacky, but makes for generally cleaner data.
            if (value == null || value.length() == 0 || value.equals("0") || value.equals("0.0") || value.equals("false")) {
                removeMeta(node, tagName);
            } else {
                setMeta(node, tagName, value);
            }
        }

        return true;
    }

    public static boolean loadTagsFromNBT(ConfigurationSection tags, Object node, String[] tagNames)
    {
        if (node == null) {
            Bukkit.getLogger().warning("Tring to load tags from a null node");
            return false;
        }
        if (!class_NBTTagCompound.isAssignableFrom(node.getClass())) {
            Bukkit.getLogger().warning("Tring to load tags from a non-CompoundTag");
            return false;
        }
        for (String tagName : tagNames)
        {
            String meta = getMeta(node, tagName);
            if (meta != null && meta.length() > 0) {
                ConfigurationUtils.set(tags, tagName, meta);
            }
        }

        return true;
    }

    public static boolean inventorySetItem(Inventory inventory, int index, ItemStack item) {
        try {
            Method setItemMethod = class_CraftInventoryCustom.getMethod("setItem", Integer.TYPE, ItemStack.class);
            setItemMethod.invoke(inventory, index, item);
            return true;
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean setInventoryResults(Inventory inventory, ItemStack item) {
        try {
            Method getResultsMethod = inventory.getClass().getMethod("getResultInventory");
            Object inv = getResultsMethod.invoke(inventory);
            Method setItemMethod = inv.getClass().getMethod("setItem", Integer.TYPE, class_ItemStack);
            setItemMethod.invoke(inv, 0, getHandle(item));
            return true;
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }
}