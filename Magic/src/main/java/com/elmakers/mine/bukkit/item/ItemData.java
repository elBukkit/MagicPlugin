package com.elmakers.mine.bukkit.item;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.google.common.collect.ImmutableSet;

public class ItemData implements com.elmakers.mine.bukkit.api.item.ItemData {
    private String key;
    private ItemStack item;
    private double worth;
    private Set<String> categories = ImmutableSet.of();
    private String creatorId;
    private String creator;

    public ItemData(ItemStack itemStack) {
        this.item = NMSUtils.getCopy(itemStack);
        this.key = itemStack.getType().toString();
    }

    public ItemData(String materialKey) throws Exception {
        MaterialAndData material = new MaterialAndData(materialKey);
        if (material.isValid()) {
            item = material.getItemStack(1);
        }
        if (item == null) {
            throw new Exception("Invalid item key: " + materialKey);
        }
        key = materialKey;
    }

    public ItemData(String key, ConfigurationSection configuration) throws Exception {
        if (configuration.isItemStack("item")) {
            item = configuration.getItemStack("item");
        } else if (configuration.isConfigurationSection("item")) {
            ConfigurationSection itemConfiguration = configuration.getConfigurationSection("item");
            String materialKey = itemConfiguration.getString("type", key);
            MaterialAndData material = new MaterialAndData(materialKey);
            if (material.isValid()) {
                item = material.getItemStack(1);
            }
            if (item == null) {
                throw new Exception("Invalid item key: " + materialKey);
            }

            ConfigurationSection tagSection = itemConfiguration.getConfigurationSection("tags");
            if (tagSection != null) {
                item = CompatibilityUtils.makeReal(item);
                InventoryUtils.saveTagsToItem(tagSection, item);
            }
        } else {
            String materialKey = configuration.getString("item", key);
            MaterialAndData material = new MaterialAndData(materialKey);
            if (material.isValid()) {
                item = material.getItemStack(1);
            }
            if (item == null) {
                throw new Exception("Invalid item key: " + materialKey);
            }
        }
        if (item == null) {
            throw new Exception("Invalid item configuration: " + key);
        }
        this.key = key;
        worth = configuration.getDouble("worth", 0);
        creator = configuration.getString("creator");
        creatorId = configuration.getString("creator_id");

        // Convenience method for renaming items
        String customName = configuration.getString("name");
        if (customName != null) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
            item.setItemMeta(meta);
        }

        Collection<String> categoriesList = ConfigurationUtils.getStringList(configuration, "categories");
        if (categoriesList != null) {
            categories = ImmutableSet.copyOf(categoriesList);
        }
    }

    public ItemData(String key, ItemStack item, double worth) throws Exception {
        if (item == null) {
            throw new Exception("Invalid item");
        }
        this.key = key;
        this.item = item;
        this.worth = worth;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public double getWorth() {
        return worth;
    }

    @Override
    public Set<String> getCategories() {
        return categories;
    }

    @Nullable
    @Override
    public ItemStack getItemStack(int amount) {
        ItemStack newItem = InventoryUtils.getCopy(item);
        if (newItem == null) {
            return null;
        }
        newItem.setAmount(amount);
        return newItem;
    }

    @Override
    public String getCreator() {
        return creator;
    }

    @Override
    public String getCreatorId() {
        return creatorId;
    }

    @Override
    public Material getType() {
        return item == null ? Material.AIR : item.getType();
    }

    @Nullable
    @Override
    public ItemMeta getItemMeta() {
        return item == null ? null : item.getItemMeta();
    }
}
