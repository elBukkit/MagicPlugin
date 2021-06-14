package com.elmakers.mine.bukkit.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.google.common.collect.ImmutableSet;

public class ItemData implements com.elmakers.mine.bukkit.api.item.ItemData, ItemUpdatedCallback {
    public static final String MINECRAFT_ITEM_PREFIX = "minecraft:";
    public static double EARN_SCALE = 0.5;

    private static class PendingUpdate {
        public ItemStack item;
        public ItemUpdatedCallback callback;

        public PendingUpdate(ItemStack item, ItemUpdatedCallback callback) {
            this.item = item;
            this.callback = callback;
        }
    }

    private final MageController controller;
    private String key;
    private String baseKey;
    private String materialKey;
    private ItemStack item;
    private ConfigurationSection configuration;
    private double worth;
    private Double earns;
    private Set<String> categories = ImmutableSet.of();
    private String creatorId;
    private String creator;
    private boolean locked;
    private boolean loaded;
    private boolean exactIngredient;
    private boolean replaceOnEquip;
    private List<PendingUpdate> pending = null;

    public ItemData(ItemStack itemStack, MageController controller) {
        this.controller = controller;
        this.item = ItemUtils.getCopy(itemStack);
        String itemKey = itemStack.getType().toString();
        if (itemStack.getAmount() > 1) {
            itemKey += "@" + itemStack.getAmount();
        }
        this.setKey(itemKey);
    }

    public ItemData(String materialKey, MageController controller) {
        this(materialKey, materialKey, controller);
    }

    public ItemData(String key, String materialKey, MageController controller) {
        this.controller = controller;
        this.setKey(key);
        this.materialKey = materialKey;
    }

    public ItemData(String key, ConfigurationSection configuration, MageController controller) {
        this.controller = controller;
        this.configuration = configuration;
        this.setKey(key);
        this.materialKey = key;

        worth = configuration.getDouble("worth", worth);
        if (configuration.contains("earns")) {
            earns = configuration.getDouble("earns");
        } else {
            earns = null;
        }
        creator = configuration.getString("creator");
        creatorId = configuration.getString("creator_id");
        locked = configuration.getBoolean("locked");
        replaceOnEquip = configuration.getBoolean("replace_on_equip");
        exactIngredient = configuration.getBoolean("exact_ingredient");

        Collection<String> categoriesList = ConfigurationUtils.getStringList(configuration, "categories");
        if (categoriesList != null) {
            categories = ImmutableSet.copyOf(categoriesList);
        }
    }

    private void createItemFromConfiguration() throws InvalidMaterialException {
        ConfigurationSection configuration = this.configuration;
        this.configuration = null;
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
                throw new InvalidMaterialException("Invalid item key: " + materialKey);
            }

            ConfigurationSection tagSection = itemConfiguration.getConfigurationSection("tags");
            if (tagSection != null) {
                item = ItemUtils.makeReal(item);
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
                throw new InvalidMaterialException("Invalid item key: " + materialKey);
            }
        }
        if (item == null) {
            throw new InvalidMaterialException("Invalid item configuration: " + key);
        }
        Collection<ConfigurationSection> attributes = ConfigurationUtils.getNodeList(configuration, "attributes");
        if (attributes != null && !attributes.isEmpty()) {
            item = ItemUtils.makeReal(item);
            for (ConfigurationSection attributeConfig : attributes) {
                String attributeKey = attributeConfig.getString("type");
                attributeKey = attributeConfig.getString("attribute", attributeKey);
                try {
                    Attribute attribute = Attribute.valueOf(attributeKey.toUpperCase());
                    double value = attributeConfig.getDouble("amount");
                    value = attributeConfig.getDouble("value", value);
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
            item = ItemUtils.makeReal(item);
            InventoryUtils.saveTagsToItem(tagSection, item);
        }
        String customName = configuration.getString("name");
        if (customName == null) {
            customName = controller.getMessages().getIfSet("items." + key + ".name");
        }
        if (customName != null) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
            item.setItemMeta(meta);
        }
        List<String> lore = configuration.getStringList("lore");
        if (lore == null) {
            lore = controller.getMessages().getAll("items." + key + ".lore");
        }
        if (lore != null && !lore.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        ConfigurationSection color = configuration.getConfigurationSection("color");
        if (color != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof LeatherArmorMeta) {
                int red = color.getInt("red");
                int green = color.getInt("green");
                int blue = color.getInt("blue");
                LeatherArmorMeta leather = (LeatherArmorMeta)meta;
                leather.setColor(Color.fromRGB(red, green, blue));
                item.setItemMeta(meta);
            }
        }
    }

    private void setKey(String key) {
        this.key = key;
        checkKey();
    }

    public void checkKey() {
        String[] pieces = StringUtils.split(key, "@", 2);
        baseKey = pieces[0];
        if (worth == 0 && pieces.length > 1) {
            try {
                int amount = Integer.parseInt(pieces[1]);
                if (amount > 1) {
                    com.elmakers.mine.bukkit.api.item.ItemData singular = controller.getItem(baseKey);
                    if (singular != null) {
                        worth = singular.getWorth() * amount;
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }

    public ItemData(String key, ItemStack item, double worth, MageController controller) throws Exception {
        this.controller = controller;
        if (item == null) {
            throw new Exception("Invalid item");
        }
        this.key = key;
        this.materialKey = key;
        this.item = item;
        this.worth = worth;
    }

    public static String cleanMinecraftItemName(String materialKey) {
        if (materialKey.startsWith(MINECRAFT_ITEM_PREFIX)) {
            materialKey = materialKey.substring(MINECRAFT_ITEM_PREFIX.length());
        }
        return materialKey;
    }

    public ItemData createVariant(String key, short damage) throws Exception {
        ItemData copy = new ItemData(key, this.item.clone(), worth, controller);
        copy.categories = categories;
        copy.item.setDurability(damage);
        return copy;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getBaseKey() {
        return baseKey;
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
        return getItemStack(amount, null);
    }

    @Nullable
    @Override
    public ItemStack getItemStack(int amount, ItemUpdatedCallback callback) {
        return getItemStack((Integer)amount, callback);
    }

    @Nullable
    @Override
    public ItemStack getItemStack() {
        return getItemStack(null, null);
    }

    @Nullable
    private ItemStack getItemStack(Integer amount, ItemUpdatedCallback callback) {
        ItemStack newItem = ItemUtils.getCopy(getOrCreateItemStack());
        if (newItem == null) {
            if (callback != null) {
                callback.updated(null);
            }
            return null;
        }
        if (pending != null) {
            pending.add(new PendingUpdate(newItem, callback));
        } else if (callback != null) {
            callback.updated(newItem);
        }
        if (amount != null) {
            newItem.setAmount(amount);
        }
        return newItem;
    }

    @Nonnull
    public ItemStack getOrCreateItemStack() {
        if (item == null) {
            if (configuration != null) {
                try {
                    createItemFromConfiguration();
                } catch (InvalidMaterialException ex) {
                    controller.info("Invalid item type '" + key + "', may not exist on your server version: " + ex.getMessage(), 2);
                }
                if (item == null) {
                    item = new ItemStack(Material.AIR);
                }
            } else {
                item = controller.createItem(materialKey, null, false, this);
                if (!loaded && InventoryUtils.isSkull(item)) {
                    pending = new ArrayList<>();
                }
                if (item == null) {
                    controller.getLogger().warning("Invalid material key: " + materialKey);
                    item = new ItemStack(Material.AIR);
                }
            }
        }
        return item;
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
        return getOrCreateItemStack().getType();
    }

    public int getCustomModelData() {
        return NBTUtils.getMetaInt(getOrCreateItemStack(), "CustomModelData", 0);
    }

    @Nullable
    @Deprecated
    @Override
    public org.bukkit.material.MaterialData getMaterialData() {
        ItemStack item = getOrCreateItemStack();
        org.bukkit.material.MaterialData materialData = item.getData();
        materialData.setData((byte)item.getDurability());
        return materialData;
    }

    @Override
    public int getDurability() {
        return getOrCreateItemStack().getDurability();
    }

    @Override
    public int getAmount() {
        return getOrCreateItemStack().getAmount();
    }

    @Nullable
    @Override
    public ItemMeta getItemMeta() {
        return getOrCreateItemStack().getItemMeta();
    }

    @Nullable
    @Override
    public MaterialAndData getMaterialAndData() {
        return new MaterialAndData(getOrCreateItemStack());
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public boolean isExactIngredient() {
        return exactIngredient;
    }

    public boolean isReplaceOnEquip() {
        return this.replaceOnEquip;
    }

    @Override
    public void updated(@Nullable ItemStack itemStack) {
        loaded = true;
        if (pending != null && itemStack != null) {
            this.item = itemStack;
            ItemMeta populatedMeta = itemStack.getItemMeta();
            Object profile = InventoryUtils.getSkullProfile(populatedMeta);
            for (PendingUpdate update : pending) {
                // We're assuming the only thing that changes here is skull profile
                if (profile != null) {
                    ItemStack item = update.item;
                    ItemMeta meta = item.getItemMeta();
                    InventoryUtils.setSkullProfile(meta, profile);
                    item.setItemMeta(meta);
                }
                if (update.callback != null) {
                    update.callback.updated(update.item);
                }
            }
        }
        pending = null;
    }

    public int getMaxDurability() {
        ItemStack itemStack = getItemStack();
        return itemStack == null ? 0 : itemStack.getType().getMaxDurability();
    }
}
