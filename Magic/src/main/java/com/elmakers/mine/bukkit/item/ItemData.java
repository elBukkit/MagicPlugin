package com.elmakers.mine.bukkit.item;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
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
    public static final String MINECRAFT_ITEM_PREFIX = "minecraft:";
    public static double EARN_SCALE = 0.5;

    private String key;
    private ItemStack item;
    private double worth;
    private Double earns;
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

    public static String cleanMinecraftItemName(String materialKey) {
        if (materialKey.startsWith(MINECRAFT_ITEM_PREFIX)) {
            materialKey = materialKey.substring(MINECRAFT_ITEM_PREFIX.length());
        }
        return materialKey;
    }

    public ItemData(String key, ConfigurationSection configuration) throws Exception {
        if (configuration.isItemStack("item")) {
            item = configuration.getItemStack("item");
        } else if (configuration.isConfigurationSection("item")) {
            ConfigurationSection itemConfiguration = configuration.getConfigurationSection("item");
            String materialKey = itemConfiguration.getString("type", key);
            materialKey = cleanMinecraftItemName(materialKey);
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
            materialKey = cleanMinecraftItemName(materialKey);
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
        if (configuration.contains("earns")) {
            earns = configuration.getDouble("earns");
        } else {
            earns = null;
        }
        creator = configuration.getString("creator");
        creatorId = configuration.getString("creator_id");

        Collection<ConfigurationSection> attributes = ConfigurationUtils.getNodeList(configuration, "attributes");
        if (attributes != null && !attributes.isEmpty()) {
            item = InventoryUtils.makeReal(item);
            for (ConfigurationSection attributeConfig : attributes) {
                String attributeKey = attributeConfig.getString("type");
                try {
                    Attribute attribute = Attribute.valueOf(attributeKey.toUpperCase());
                    double value = attributeConfig.getDouble("amount");
                    String slot = attributeConfig.getString("slot");
                    String uuidString = attributeConfig.getString("uuid");
                    UUID uuid = null;
                    if (uuidString != null) {
                        try {
                            uuid = UUID.fromString(uuidString);
                        } catch (Exception ignore) {

                        }
                    }
                    if (uuid == null) {
                        uuid = UUID.randomUUID();
                    }
                    int operation = attributeConfig.getInt("operation", 0);
                    if (!CompatibilityUtils.setItemAttribute(item, attribute, value, slot, operation, uuid)) {
                        Bukkit.getLogger().warning("Failed to set attribute: " + attributeKey);
                    }
                } catch (Exception ex) {
                     Bukkit.getLogger().warning("Invalid attribute: " + attributeKey);
                }
            }
        } else {
            ConfigurationSection simpleAttributes = configuration.getConfigurationSection("attributes");
            if (simpleAttributes != null) {
                InventoryUtils.applyAttributes(item, simpleAttributes, configuration.getString("attribute_slot"));
            }
        }

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
        if (configuration.getBoolean("locked")) {
            locked = true;
            InventoryUtils.setMetaBoolean(item, "locked", true);
            InventoryUtils.setMeta(item, "magic_key", key);
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
    public double getEarns() {
        return earns == null ? worth * EARN_SCALE : earns;
    }

    @Override
    public boolean hasCustomEarns() {
        return earns != null;
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

    @Override
    public int getDurability() {
        return item == null ? 0 : item.getDurability();
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
