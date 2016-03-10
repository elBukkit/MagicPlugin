package com.elmakers.mine.bukkit.magic.listener;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.item.MagicItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemController implements Listener {
    private MageController controller;
    private final Map<String, MagicItem> items = new HashMap<String, MagicItem>();
    private final Map<ItemStack, MagicItem> itemsByStack = new HashMap<ItemStack, MagicItem>();
    
    public ItemController(MageController controller) {
        this.controller = controller;
    }
    
    public void load(ConfigurationSection configuration) {
        Set<String> itemKeys = configuration.getKeys(false);
        for (String itemKey : itemKeys) {
            ConfigurationSection itemConfiguration = configuration.getConfigurationSection(itemKey);
            ItemStack item = null;
            if (itemConfiguration.isItemStack("item") ) {
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
            MagicItem magicItem = new MagicItem(itemKey, item, worth);
            items.put(itemKey, magicItem);
            itemsByStack.put(item, magicItem);
        }
    }
    
    public int getCount() {
        return items.size();
    }

    public Set<String> getKeys() {
        return items.keySet();
    }
    
    public MagicItem get(String key) {
        return items.get(key);
    }

    public MagicItem get(ItemStack item) {
        return itemsByStack.get(item);
    }
}
