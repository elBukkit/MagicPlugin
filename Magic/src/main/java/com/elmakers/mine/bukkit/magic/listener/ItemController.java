package com.elmakers.mine.bukkit.magic.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.item.ItemData;

public class ItemController {
    private MageController controller;
    private final Map<String, ItemData> items = new HashMap<>();
    private final Map<ItemStack, ItemData> itemsByStack = new HashMap<>();

    public ItemController(MageController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection configuration) {
        Set<String> itemKeys = configuration.getKeys(false);
        for (String itemKey : itemKeys) {
            ConfigurationSection itemConfig = configuration.getConfigurationSection(itemKey);
            if (itemConfig != null) {
                loadItem(itemKey, itemConfig);
            } else {
                controller.getLogger().warning("Improperly formatted item: " + itemKey);
            }
        }
    }

    public void loadItem(String itemKey, ConfigurationSection configuration) {
        try {
            ItemData magicItem = new ItemData(itemKey, configuration);
            items.put(itemKey, magicItem);
            itemsByStack.put(magicItem.getItemStack(1), magicItem);
        } catch (Throwable ex) {
            controller.getLogger().log(Level.WARNING, "An error occurred while processing the item: " + itemKey, ex);
        }
    }

    public int getCount() {
        return items.size();
    }

    public Set<String> getKeys() {
        return items.keySet();
    }

    public ItemData get(String key) {
        return items.get(key);
    }

    public ItemData get(ItemStack item) {
        return itemsByStack.get(item);
    }

    public ItemData getOrCreate(String key) {
        ItemData data = get(key);
        if (data == null) {
            Wand wand = controller.createWand(key);
            if (wand != null) {
                data = new ItemData(wand.getItem());
            } else {
                try {
                    data = new ItemData(key);
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Error creating item: " + key);
                }
            }
        }
        return data;
    }

    public void remove(String key) {
        ItemData existing = items.get(key);
        if (existing != null) {
            itemsByStack.remove(existing.getItemStack(1));
        }
        items.remove(key);
    }
}
