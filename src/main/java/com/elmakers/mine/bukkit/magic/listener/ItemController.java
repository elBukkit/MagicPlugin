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
            try {
                ConfigurationSection itemConfiguration = configuration.getConfigurationSection(itemKey);
                ItemStack item = null;
                if (itemConfiguration.isItemStack("item")) {
                    item = itemConfiguration.getItemStack("item");
                } else {
                    String materialKey = itemConfiguration.getString("item", itemKey);
                    MaterialAndData material = new MaterialAndData(materialKey);
                    if (material.isValid()) {
                        item = material.getItemStack(1);
                    }
                    if (item == null) {
                        controller.getLogger().warning("Invalid item key: " + materialKey);
                        continue;
                    }
                }
                if (item == null) {
                    controller.getLogger().warning("Invalid item configuration: " + itemKey);
                    continue;
                }
                double worth = itemConfiguration.getDouble("worth", 0);
                ItemData magicItem = new ItemData(itemKey, item, worth);
                items.put(itemKey, magicItem);
                itemsByStack.put(item, magicItem);
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "An error occurred while processing the item: " + itemKey, ex);
            }
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
}
