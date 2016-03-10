package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.item.ItemData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class ItemController implements Listener {
    private MageController controller;
    private final Map<String, ItemData> items = new HashMap<String, ItemData>();
    private final Map<ItemStack, ItemData> itemsByStack = new HashMap<ItemStack, ItemData>();
    
    public ItemController(MageController controller) {
        this.controller = controller;
    }
    
    public void load(ConfigurationSection configuration) {
        Set<String> itemKeys = configuration.getKeys(false);
        for (String itemKey : itemKeys) {
            loadItem(itemKey, configuration.getConfigurationSection(itemKey));
        }
    }
    
    public void loadItem(String itemKey, ConfigurationSection configuration) {
        try {
            ItemData magicItem = new ItemData(itemKey, configuration);
            items.put(itemKey, magicItem);
            itemsByStack.put(magicItem.getItemStack(1), magicItem);
        } catch (Exception ex) {
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

    public void remove(String key) {
        ItemData existing = items.get(key);
        if (existing != null) {
            itemsByStack.remove(existing.getItemStack(1));
        }
        items.remove(key);
    }
}
