package com.elmakers.mine.bukkit.item;

import java.util.Collection;
import java.util.List;
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
import com.elmakers.mine.bukkit.wand.Wand;
import com.google.common.collect.ImmutableSet;

public class ItemData implements com.elmakers.mine.bukkit.api.item.ItemData {
    private String key;
    private ItemStack item;
    private double worth;
    private Set<String> categories = ImmutableSet.of();
    private String creatorId;
    private String creator;
    private boolean locked;

    public ItemData(ItemStack itemStack) {
        this.item = NMSUtils.getCopy(itemStack);
        this.key = itemStack.getType().toString();
        locked = (Wand.getLockKey(itemStack) != null);
    }

    public ItemData(String materialKey) throws Exception {
        MaterialAndData material = new MaterialAndData(materialKey);
        if (material.isValid() && CompatibilityUtils.isLegacy(material.getMaterial())) {
            short convertData = (material.getData() == null ? 0 : material.getData());
            material = new MaterialAndData(CompatibilityUtils.migrateMaterial(material.getMaterial(), (byte)convertData));
        }
        if (material.isValid()) {
            item = material.getItemStack(1);
        }
        if (item == null) {
            throw new Exception("Invalid item key: " + materialKey);
        }
        key = materialKey;
    }

    public ItemData(String key, String materialKey) throws Exception {
        this.key = key;
        MaterialAndData material = new MaterialAndData(materialKey);
        if (material.isValid()) {
            item = material.getItemStack(1);
        }
        if (item == null) {
            throw new Exception("Invalid item key: " + materialKey);
        }
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

        // Convenience methods for top-level name, lore and tags
        ConfigurationSection tagSection = configuration.getConfigurationSection("tags");
        if (tagSection != null) {
            item = CompatibilityUtils.makeReal(item);
            InventoryUtils.saveTagsToItem(tagSection, item);
        }
        String customName = configuration.getString("name");
        if (customName != null) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
            item.setItemMeta(meta);
        }
        List<String> lore = configuration.getStringList("lore");
        if (lore != null && !lore.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        InventoryUtils.setMeta(item, "magic_key", key);
        if (configuration.getBoolean("locked")) {
            locked = true;
            InventoryUtils.setMetaBoolean(item, "locked", true);
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

    public ItemData createVariant(String key, short damage) throws Exception {
        ItemData copy = new ItemData(key, this.item.clone(), worth);
        copy.categories = categories;
        copy.item.setDurability(damage);
        return copy;
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

    @Nullable
    @Override
    public ItemStack getItemStack() {
        return getItemStack(1);
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
    @Deprecated
    @Override
    public org.bukkit.material.MaterialData getMaterialData() {
        if (item == null) return null;
        org.bukkit.material.MaterialData materialData = item.getData();
        materialData.setData((byte)item.getDurability());
        return materialData;
    }

    @Nullable
    @Override
    public ItemMeta getItemMeta() {
        return item == null ? null : item.getItemMeta();
    }

    @Nullable
    @Override
    public MaterialAndData getMaterialAndData() {
        if (item == null) {
            return null;
        }

        return new MaterialAndData(item);
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }
}
