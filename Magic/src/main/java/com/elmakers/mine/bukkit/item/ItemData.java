package com.elmakers.mine.bukkit.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.PlayerProfile;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.google.common.collect.ImmutableSet;

public class ItemData implements com.elmakers.mine.bukkit.api.item.ItemData, ItemUpdatedCallback, Cloneable {
    public static final String MINECRAFT_ITEM_PREFIX = "minecraft:";
    public static double EARN_SCALE = 0.5;
    private static final String[] BOOLEAN_FLAGS = {"temporary", "unstashable", "undroppable", "unswappable", "unmoveable"};

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
    private Integer damage;
    private Set<String> categories = ImmutableSet.of();
    private String creatorId;
    private String creator;
    private boolean cache = true;
    private boolean locked;
    private boolean loaded;
    private boolean exactIngredient;
    private boolean replaceOnEquip;
    private List<String> discoverRecipes;
    private List<PendingUpdate> pending = null;

    public ItemData(ItemStack itemStack, MageController controller) {
        this.controller = controller;
        this.item = CompatibilityLib.getItemUtils().getCopy(itemStack);
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
        discoverRecipes = ConfigurationUtils.getStringList(configuration, "discover_recipes");
        damage = ConfigurationUtils.getOptionalInteger(configuration, "damage");
        cache = configuration.getBoolean("cache", true);
        // Slightly more efficient if this has been overridden to an empty list
        if (discoverRecipes != null && discoverRecipes.isEmpty()) {
            discoverRecipes = null;
        }

        Collection<String> categoriesList = ConfigurationUtils.getStringList(configuration, "categories");
        if (categoriesList != null) {
            categories = ImmutableSet.copyOf(categoriesList);
        }
    }

    private ItemStack createItemFromConfiguration() throws InvalidMaterialException {
        SkinUtils skinUtils = CompatibilityLib.getSkinUtils();
        CompatibilityUtils compatibilityUtils = CompatibilityLib.getCompatibilityUtils();
        NBTUtils nbtUtils = CompatibilityLib.getNBTUtils();
        ConfigurationSection configuration = this.configuration;
        // Save this configuration for later if we're not caching the item, otherwise we are done with it.
        if (cache) {
            this.configuration = null;
        }
        ItemStack item = null;
        if (configuration.isItemStack("item")) {
            item = configuration.getItemStack("item");
        } else if (configuration.isConfigurationSection("item")) {
            ConfigurationSection itemConfiguration = configuration.getConfigurationSection("item");
            if (itemConfiguration.getBoolean("bukkit")) {
                // See note below on why this is serialized this way.
                // This works around a huge headache with builtin serialized items and
                // backwards compatibility.
                // It is super hacky, but it works!
                YamlConfiguration itemConfig = new YamlConfiguration();
                itemConfig.set("item", itemConfiguration);
                String itemConfigString = itemConfig.saveToString();
                itemConfigString = itemConfigString.replace("bukkit: true", "==: org.bukkit.inventory.ItemStack");
                try {
                    itemConfig.loadFromString(itemConfigString);
                    item = itemConfig.getItemStack("item");
                } catch (InvalidConfigurationException ex) {
                    controller.getLogger().log(Level.WARNING, "Failed to convert load serialized item from config", ex);
                }
            } else {
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
                    item = CompatibilityLib.getItemUtils().makeReal(item);
                    nbtUtils.saveTagsToItem(tagSection, item);
                }
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
            item = CompatibilityLib.getItemUtils().makeReal(item);
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
                    String operation = attributeConfig.getString("operation");
                    if (!CompatibilityLib.getCompatibilityUtils().setItemAttribute(item, attribute, value, slot, operation, uuid)) {
                        Bukkit.getLogger().warning("Failed to set attribute: " + attributeKey);
                    }
                } catch (Exception ex) {
                     Bukkit.getLogger().warning("Invalid attribute: " + attributeKey);
                }
            }
        } else {
            ConfigurationSection simpleAttributes = configuration.getConfigurationSection("attributes");
            if (simpleAttributes != null) {
                CompatibilityLib.getInventoryUtils().applyAttributes(item, simpleAttributes, configuration.getString("attribute_slot"));
            }
        }

        // Convenience methods for top-level name, lore and tags
        // Actually, let's just do everything this way now.

        // Perform non-meta operations first so the changes are already in the item meta
        ConfigurationSection tagSection = configuration.getConfigurationSection("tags");
        if (tagSection != null) {
            item = CompatibilityLib.getItemUtils().makeReal(item);
            nbtUtils.saveTagsToItem(tagSection, item);
        }
        for (String flag : BOOLEAN_FLAGS) {
            if (configuration.contains(flag)) {
                item = CompatibilityLib.getItemUtils().makeReal(item);
                nbtUtils.setBoolean(item, flag, configuration.getBoolean(flag));
            }
        }
        int customModelData = configuration.getInt("custom_model_data");
        if (customModelData > 0) {
            CompatibilityLib.getItemUtils().setCustomModelData(item, customModelData);
        }

        // Only ItemMeta operations from here
        ItemMeta itemMeta = item.getItemMeta();
        String customName = configuration.getString("name");
        if (customName == null) {
            customName = controller.getMessages().getIfSet("items." + key + ".name");
        }
        if (customName != null) {
            itemMeta.setDisplayName(CompatibilityLib.getCompatibilityUtils().translateColors(customName));
        }
        List<String> lore = configuration.getStringList("lore");
        if (lore == null) {
            lore = controller.getMessages().getAll("items." + key + ".lore");
        }
        if (lore != null && !lore.isEmpty()) {
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, CompatibilityLib.getCompatibilityUtils().translateColors(lore.get(i)));
            }
            itemMeta.setLore(lore);
        }
        ConfigurationSection colorConfig = configuration.getConfigurationSection("color");
        if (colorConfig != null) {
            int red = colorConfig.getInt("red");
            int green = colorConfig.getInt("green");
            int blue = colorConfig.getInt("blue");
            final Color color = Color.fromRGB(red, green, blue);
            if (itemMeta instanceof LeatherArmorMeta) {
                LeatherArmorMeta leather = (LeatherArmorMeta)itemMeta;
                leather.setColor(color);
            } else if (itemMeta instanceof PotionMeta) {
                PotionMeta potion = (PotionMeta)itemMeta;
                potion.setColor(color);
            }
        }
        ConfigurationSection potionEffects = configuration.getConfigurationSection("potion_effects");
        if (potionEffects != null) {
            if (itemMeta instanceof PotionMeta) {
                PotionMeta potion = (PotionMeta)itemMeta;
                int potionEffectDuration = configuration.getInt("potion_effect_duration");
                Collection<PotionEffect> effects = ConfigurationUtils.getPotionEffects(potionEffects, potionEffectDuration);
                if (effects != null && !effects.isEmpty()) {
                    for (PotionEffect effect : effects) {
                        potion.addCustomEffect(effect, true);
                    }
                }
            }
        }

        // Enchantments can present as a list or a section
        final Map<Enchantment, Integer> enchantments = new HashMap<>();
        ConfigurationSection enchantSection = configuration.getConfigurationSection("enchantments");
        if (enchantSection != null) {
            for (String enchantKey : enchantSection.getKeys(false)) {
                Enchantment enchantment = compatibilityUtils.getEnchantmentByKey(enchantKey);
                if (enchantment == null) {
                    controller.getLogger().warning("Invalid enchantment: " + enchantKey);
                    continue;
                }
                enchantments.put(enchantment, enchantSection.getInt(enchantKey));
            }
        } else {
            List<String> enchantList = configuration.getStringList("enchantments");
            if (enchantList != null) {
                for (String enchantKey : enchantList) {
                    int level = 1;
                    String[] pieces = StringUtils.split(enchantKey, ":");
                    if (pieces.length > 1) {
                        try {
                            level = Integer.parseInt(pieces[pieces.length - 1]);
                            String[] keyPieces = Arrays.copyOf(pieces, pieces.length - 1);
                            enchantKey = StringUtils.join(keyPieces, ":");
                        } catch (Exception ignore) {
                        }
                    }
                    Enchantment enchantment = compatibilityUtils.getEnchantmentByKey(enchantKey);
                    if (enchantment == null) {
                        controller.getLogger().warning("Invalid enchantment: " + enchantKey);
                        continue;
                    }
                    enchantments.put(enchantment, level);
                }
            }
        }
        if (!enchantments.isEmpty()) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        int damage = configuration.getInt("damage");
        if (damage > 0) {
            if (itemMeta instanceof Damageable) {
                Damageable damageable = (Damageable)itemMeta;
                damageable.setDamage(damage);
            }
        }

        ConfigurationSection playerConfig = configuration.getConfigurationSection("player");
        if (playerConfig != null) {
            if (itemMeta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta)itemMeta;
                PlayerProfile playerProfile = skinUtils.parsePlayerProfile(playerConfig);
                playerProfile.update(skullMeta);
            }
        }

        if (configuration.getBoolean("unbreakable")) {
            itemMeta.setUnbreakable(true);
        }
        List<String> flagKeys = configuration.getStringList("flags");
        for (String flagKey : flagKeys) {
            try {
                itemMeta.addItemFlags(ItemFlag.valueOf(flagKey.toUpperCase()));
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid flag: " + flagKey);
            }
        }

        // Version-specific data handling
        CompatibilityLib.getItemUtils().loadMeta(controller, itemMeta, configuration);

        // Finally apply item meta changes
        item.setItemMeta(itemMeta);

        return item;
    }

    // Returns an item with any unsaved data in it
    @Override
    public ItemStack save(ConfigurationSection configuration) {
        NBTUtils nbtUtils = CompatibilityLib.getNBTUtils();
        ItemStack itemStack = getItemStack();
        configuration.set("item", itemStack.getType().name().toLowerCase());
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasDisplayName()) {
            configuration.set("name", itemMeta.getDisplayName());
            itemMeta.setDisplayName(null);
        }
        if (itemMeta.hasLore()) {
            configuration.set("lore", itemMeta.getLore());
            itemMeta.setLore(null);
        }
        if (itemMeta.hasCustomModelData()) {
            configuration.set("custom_model_data", itemMeta.getCustomModelData());
            itemMeta.setCustomModelData(null);
        }
        if (itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leather = (LeatherArmorMeta)itemMeta;
            ConfigurationSection colorSection = configuration.createSection("color");
            Color color = leather.getColor();
            colorSection.set("red", color.getRed());
            colorSection.set("green", color.getGreen());
            colorSection.set("blue", color.getBlue());
            leather.setColor(null);
        }
        if (itemMeta.isUnbreakable()) {
            configuration.set("unbreakable", true);
            itemMeta.setUnbreakable(false);
        }

        for (String flag : BOOLEAN_FLAGS) {
            if (nbtUtils.containsTag(itemStack, flag)) {
                configuration.set(flag, nbtUtils.getBoolean(itemStack, flag, false));
                nbtUtils.removeMeta(itemStack, flag);
            }
        }

        ConfigurationSection tagSection = configuration.createSection("tags");
        if (!ConfigurationUtils.loadAllTagsFromNBT(tagSection, itemStack) || tagSection.getKeys(false).isEmpty()) {
            configuration.set("tags", null);
        }

        if (itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable)itemMeta;
            if (damageable.hasDamage()) {
                configuration.set("damage", damageable.getDamage());
                damageable.setDamage(0);
            }
        }

        Set<ItemFlag> flags = itemMeta.getItemFlags();
        if (!flags.isEmpty()) {
            List<String> flagIds = new ArrayList<>();
            for (ItemFlag flag : flags) {
                flagIds.add(flag.name());
                itemMeta.removeItemFlags(flag);
            }
            configuration.set("flags", flagIds);
        }

        // Version-specific data handling
        CompatibilityLib.getItemUtils().saveMeta(controller, itemMeta, configuration);

        itemStack.setItemMeta(itemMeta);

        // Always remove custom data, this is saved in the tags section
        // Do this after resetting item meta since the custom data is stored there, too
        CompatibilityLib.getItemUtils().removeCustomData(itemStack);

        if (itemStack.hasItemMeta()) {
            // Save the remainder as a serialized item config
            // Not an actual serialized item though because then we can't layer configs over it
            // without bukkit throwing errors
            YamlConfiguration itemConfig = new YamlConfiguration();
            itemConfig.set("item", itemStack);

            // This is so hacky, yo
            String itemConfigString = itemConfig.saveToString();
            itemConfigString = itemConfigString.replace("==: org.bukkit.inventory.ItemStack", "bukkit: true");
            try {
                itemConfig.loadFromString(itemConfigString);
                configuration.set("item", itemConfig.getConfigurationSection("item"));
            } catch (InvalidConfigurationException ex) {
                controller.getLogger().log(Level.WARNING, "Failed to convert item to bukkit serialized format, saving as serialized item instead", ex);
                configuration.set("item", itemStack);
            }
        }
        return itemStack;
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
        ItemData variant = (ItemData)this.clone();
        variant.damage = (int)damage;
        variant.key = key;
        variant.materialKey = key;
        if (variant.item != null) {
            variant.item = variant.item.clone();
            CompatibilityLib.getDeprecatedUtils().setItemDamage(variant.item, damage);
        }
        return variant;
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
        ItemStack newItem = CompatibilityLib.getItemUtils().getCopy(getOrCreateItemStack());
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
        if (item == null || !cache) {
            if (configuration != null) {
                try {
                    item = createItemFromConfiguration();
                } catch (InvalidMaterialException ex) {
                    controller.info("Invalid item type '" + key + "', may not exist on your server version: " + ex.getMessage(), 2);
                }
                if (item == null) {
                    item = new ItemStack(Material.AIR);
                }
            } else {
                try {
                    item = controller.createForItemData(materialKey, this);
                } catch (Exception ex) {
                    controller.info("There was an error creating an item of type: " + materialKey);
                }
                if (!loaded && CompatibilityLib.getInventoryUtils().isSkull(item)) {
                    pending = new ArrayList<>();
                }
                if (item == null) {
                    controller.getLogger().warning("Invalid item key: " + materialKey);
                    item = new ItemStack(Material.AIR);
                }
            }
            if (item != null && damage != null) {
                CompatibilityLib.getDeprecatedUtils().setItemDamage(item, (short)(int)damage);
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
        return CompatibilityLib.getItemUtils().getCustomModelData(getOrCreateItemStack());
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
        return CompatibilityLib.getDeprecatedUtils().getItemDamage(getOrCreateItemStack());
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
            PlayerProfile profile = null;
            if (populatedMeta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta)populatedMeta;
                profile = CompatibilityLib.getSkinUtils().getPlayerProfile(skullMeta);
            }
            for (PendingUpdate update : pending) {
                // We're assuming the only thing that changes here is skull profile
                if (profile != null) {
                    ItemStack item = update.item;
                    ItemMeta updateMeta = item.getItemMeta();
                    if (updateMeta instanceof SkullMeta) {
                        SkullMeta updateSkullMeta = (SkullMeta)updateMeta;
                        profile.update(updateSkullMeta);
                        item.setItemMeta(updateMeta);
                    }
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

    @Nullable
    @Override
    public Collection<String> getDiscoverRecipes() {
        return discoverRecipes;
    }

    @Override
    public void addDiscoverRecipe(String recipe) {
        if (discoverRecipes == null) {
            discoverRecipes = new ArrayList<>();
        }
        discoverRecipes.add(recipe);
    }

    @Override
    public void applyToItem(ItemStack itemStack) {
        ItemStack thisItem = getOrCreateItemStack();
        itemStack.setType(thisItem.getType());
        itemStack.setItemMeta(thisItem.getItemMeta());
    }
}
