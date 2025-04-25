package com.elmakers.mine.bukkit.utility.platform.base;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import com.elmakers.mine.bukkit.utility.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.CurrencyAmount;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public class InventoryUtilsBase implements InventoryUtils {
    protected final Platform platform;

    protected InventoryUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    @Override
    public CurrencyAmount getCurrencyAmount(ItemStack item) {
        if (platform.getItemUtils().isEmpty(item)) return null;

        Object currency = platform.getNBTUtils().getTag(item, "currency");
        if (currency != null) {
            String currencyType = platform.getNBTUtils().getString(currency, "type");
            if (currencyType != null) {
                return new CurrencyAmount(currencyType, platform.getNBTUtils().getOptionalInt(currency, "amount"));
            }
            return null;
        }

        // Support for legacy SP items
        int spAmount = platform.getNBTUtils().getInt(item, "sp", 0);
        if (spAmount > 0) {
            return new CurrencyAmount("sp", spAmount);
        }
        return null;
    }

    @Override
    public boolean configureSkillItem(ItemStack skillItem, String skillClass, boolean quickCast, ConfigurationSection skillConfig) {
        if (skillItem == null) return false;
        Object handle = platform.getItemUtils().getHandle(skillItem);
        if (handle == null) return false;
        Object tag = platform.getItemUtils().getOrCreateTag(handle);
        if (tag == null) return false;

        platform.getNBTUtils().setBoolean(tag, "skill", true);

        Object spellNode = platform.getNBTUtils().getTag(skillItem, "spell");
        if (skillClass != null && spellNode != null) {
            platform.getNBTUtils().setString(spellNode, "class", skillClass);
        }
        if (skillConfig == null) {
            return true;
        }

        if (skillConfig.getBoolean("undroppable", false)) {
            platform.getNBTUtils().setBoolean(tag, "undroppable", true);
        }
        if (skillConfig.getBoolean("keep", false)) {
            platform.getNBTUtils().setBoolean(tag, "keep", true);
        }
        String quickCastString = skillConfig.getString("quick_cast", "");
        if (!quickCastString.equalsIgnoreCase("auto")) {
            quickCast = skillConfig.getBoolean("quick_cast", true);
        }
        if (!quickCast && spellNode != null) {
            platform.getNBTUtils().setBoolean(spellNode, "quick_cast", false);
        }

        return true;
    }

    @Override
    public ItemStack setSkullURL(ItemStack itemStack, String url) {
        try {
            // Using a deterministic non-random UUID derived from the skull url here so skulls of the same type can stack
            return setSkullURL(itemStack, new URL(url), UUID.nameUUIDFromBytes(url.getBytes()));
        } catch (MalformedURLException e) {
            platform.getLogger().log(Level.WARNING, "Malformed URL: " + url, e);
        }
        return itemStack;
    }

    @Override
    public ItemStack setSkullURL(ItemStack itemStack, URL url, UUID id) {
        // Old versions of Bukkit would NPE trying to save a skull without an owner name
        // So we'll use MHF_Question, why not.
        return setSkullURL(itemStack, url, id, "MHF_Question");
    }

    @Override
    public ItemStack setSkullURL(ItemStack itemStack, URL url, UUID id, String ownerName) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta)itemMeta;
            PlayerProfile fakeProfile = Bukkit.createPlayerProfile(id, ownerName);
            fakeProfile.getTextures().setSkin(url);
            skullMeta.setOwnerProfile(fakeProfile);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    @Override
    public String getSkullURL(ItemStack skull) {
        ItemMeta itemMeta = skull.getItemMeta();
        if (itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta)itemMeta;
            PlayerProfile playerProfile = skullMeta.getOwnerProfile();
            URL skinURL = playerProfile == null ? null : playerProfile.getTextures().getSkin();
            if (skinURL != null) {
                return skinURL.toString();
            }
        }
        return null;
    }

    @Override
    public boolean isSkull(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta instanceof SkullMeta;
    }

    @Override
    public void makeKeep(ItemStack itemStack) {
        platform.getNBTUtils().setBoolean(itemStack, "keep", true);
    }

    @Override
    public boolean isKeep(ItemStack itemStack) {
        return platform.getNBTUtils().containsTag(itemStack, "keep");
    }

    @Override
    public void applyAttributes(ItemStack item, ConfigurationSection attributeConfig, String slot) {
        if (item == null) return;
        platform.getCompatibilityUtils().removeItemAttributes(item);
        if (attributeConfig == null) return;
        Collection<String> attributeKeys = attributeConfig.getKeys(false);
        for (String attributeKey : attributeKeys)
        {
            // Note that there is some duplication here with BaseMageModifier.getAttributeModifiers
            // We want to keep the syntax the same, however there are some fundamental differences in how
            // entity vs item modifiers work, enough that it makes sense to keep the two separate
            try {
                double value = 0;
                int operation = 0;
                UUID uuid = null;
                ConfigurationSection attributeConfiguration = attributeConfig.getConfigurationSection(attributeKey);
                if (attributeConfiguration != null) {
                    attributeKey = attributeConfiguration.getString("attribute", attributeKey);
                    value = attributeConfiguration.getDouble("value");
                    slot = attributeConfiguration.getString("slot", slot);
                    String operationKey = attributeConfiguration.getString("operation");
                    if (operationKey != null && !operationKey.isEmpty()) {
                        try {
                            AttributeModifier.Operation eOperation = AttributeModifier.Operation.valueOf(operationKey.toUpperCase());
                            operation = eOperation.ordinal();
                        } catch (Exception ex) {
                            platform.getLogger().warning("Invalid operation " + operationKey);
                        }
                    }
                    String uuidString = attributeConfiguration.getString("uuid");
                    if (uuidString != null && !uuidString.isEmpty()) {
                        try {
                            uuid = UUID.fromString(uuidString);
                        } catch (Exception ex) {
                            platform.getLogger().warning("Invalid UUID " + uuidString);
                        }
                    }
                } else {
                    value = attributeConfig.getDouble(attributeKey);
                }
                if (uuid == null) {
                    uuid = UUID.randomUUID();
                }
                Attribute attribute = Attribute.valueOf(attributeKey.toUpperCase());
                if (!platform.getCompatibilityUtils().setItemAttribute(item, attribute, value, slot, operation, uuid)) {
                    platform.getLogger().warning("Failed to set attribute: " + attributeKey);
                }
            } catch (Exception ex) {
                platform.getLogger().warning("Invalid attribute: " + attributeKey);
            }
        }
    }

    @Override
    public void applyEnchantments(ItemStack item, ConfigurationSection enchantConfig) {
        if (item == null) return;

        Set<Enchantment> keep = null;
        if (enchantConfig != null) {
            keep = new HashSet<>();
            CompatibilityUtils compatibilityUtils = platform.getCompatibilityUtils();
            Collection<String> enchantKeys = enchantConfig.getKeys(false);
            for (String enchantKey : enchantKeys)
            {
                try {
                    Enchantment enchantment = compatibilityUtils.getEnchantmentByKey(enchantKey);
                    if (enchantment == null) {
                        platform.getLogger().warning("Invalid enchantment: " + enchantKey);
                        continue;
                    }
                    item.addUnsafeEnchantment(enchantment, enchantConfig.getInt(enchantKey));
                    keep.add(enchantment);
                } catch (Exception ex) {
                    platform.getLogger().log(Level.SEVERE, "Error adding enchantment to item " + item.getType() + ": " + enchantKey, ex);
                }
            }
        }
        Collection<Enchantment> existing = new ArrayList<>(item.getEnchantments().keySet());
        for (Enchantment enchantment : existing) {
            if (keep == null || !keep.contains(enchantment)) {
                item.removeEnchantment(enchantment);
            }
        }
    }

    @Override
    public boolean addEnchantments(ItemStack item, ConfigurationSection enchantConfig) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        boolean addedAny = false;
        if (enchantConfig != null) {
            CompatibilityUtils compatibilityUtils = platform.getCompatibilityUtils();
            Collection<String> enchantKeys = enchantConfig.getKeys(false);
            for (String enchantKey : enchantKeys) {
                try {
                    Enchantment enchantment = compatibilityUtils.getEnchantmentByKey(enchantKey);
                    if (enchantment == null) {
                        platform.getLogger().warning("Invalid enchantment: " + enchantKey);
                        continue;
                    }
                    int level = enchantConfig.getInt(enchantKey);
                    if (meta.getEnchantLevel(enchantment) >= level) continue;
                    if (!meta.hasEnchant(enchantment) && meta.hasConflictingEnchant(enchantment)) continue;
                    if (meta.addEnchant(enchantment, level, false)) {
                        addedAny = true;
                    }
                } catch (Exception ex) {
                    platform.getLogger().log(Level.SEVERE, "Error adding enchantment to item " + item.getType() + ": " + enchantKey, ex);
                }
            }
        }
        if (addedAny) {
            item.setItemMeta(meta);
        }
        return addedAny;
    }

    @Override
    public String describeProperty(Object property) {
        return describeProperty(property, 0);
    }

    @Override
    public String describeProperty(Object property, int maxLength) {
        if (property == null) return "(Empty)";
        String propertyString;
        if (property instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection)property;
            Set<String> keys = section.getKeys(false);
            StringBuilder full = new StringBuilder("{");
            boolean first = true;
            for (String key : keys) {
                if (!first) {
                    full.append(',');
                }
                first = false;
                full.append(key).append(':').append(describeProperty(section.get(key)));
            }
            propertyString = full.append('}').toString();
        } else {
            propertyString = property.toString();
        }
        if (maxLength > 0 && propertyString.length() > maxLength - 3) {
            propertyString = propertyString.substring(0, maxLength - 3) + "...";
        }
        return propertyString;
    }

    @Override
    @SuppressWarnings("EqualsReference")
    public boolean isSameInstance(ItemStack one, ItemStack two) {
        return one == two;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getMapId(ItemStack mapItem) {
        ItemMeta meta = mapItem.getItemMeta();
        if (meta instanceof MapMeta) {
            MapMeta mapMeta = (MapMeta)meta;
            // Why is this deprecated if there is no alternative?
            return mapMeta.getMapId();
        }
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setMapId(ItemStack mapItem, int id) {
        ItemMeta meta = mapItem.getItemMeta();
        if (meta instanceof MapMeta) {
            MapMeta mapMeta = (MapMeta)meta;
            // Why is this deprecated if there is no alternative?
            mapMeta.setMapId(id);
            mapItem.setItemMeta(mapMeta);
        }
    }

    @Override
    public ItemStack createMap(Material material, int mapId) {
        ItemStack mapItem = new ItemStack(material, 1);
        setMapId(mapItem, mapId);
        return mapItem;
    }

    @Override
    public void wrapText(String text, Collection<String> list) {
        wrapText(text, CompatibilityConstants.MAX_LORE_LENGTH, list);
    }

    @Override
    public void wrapText(String text, String prefix, Collection<String> list) {
        wrapText(text, prefix, CompatibilityConstants.MAX_LORE_LENGTH, list);
    }

    @Override
    public void wrapText(String text, int maxLength, Collection<String> list) {
        wrapText(text, "", maxLength, list);
    }

    @Override
    public void wrapText(String text, String prefix, int maxLength, Collection<String> list) {
        if (text == null || text.isEmpty()) return;
        String colorPrefix = "";
        String[] lines = StringUtils.splitPreserveAllTokens(text, "\n\r");
        if (maxLength == 0) {
            list.addAll(Arrays.asList(lines));
            return;
        }
        final String wrapPrefix = CompatibilityConstants.LORE_WRAP_PREFIX;
        for (String line : lines) {
            if (line.isEmpty()) {
                list.add(line);
                continue;
            }
            line = prefix + line;

            // Parse escaped chat components
            String[] components = StringUtils.splitPreserveAllTokens(line, "`");
            StringBuilder currentLine = new StringBuilder();
            int currentLength = 0;
            for (int i = 0; i < components.length; i++) {
                String component = components[i];
                // Don't break up components at all
                if (component.startsWith("{")) {
                    // Get cleaned length
                    int length = ChatColor.stripColor(ChatUtils.getSimpleMessage(component)).length();
                    // If this can not fit on the current line, map a new one
                    if (currentLength != 0 && currentLength + length > maxLength) {
                        // Add current line to lore, start new line
                        list.add(colorPrefix + currentLine);
                        currentLine.setLength(0);
                        currentLine.append(wrapPrefix);
                        currentLength = wrapPrefix.length();
                    }
                    // Re-escape, we parsed this away in the split()
                    currentLine.append('`');
                    currentLine.append(component);
                    currentLine.append('`');
                    // Track cleaned length
                    currentLength += length;
                    continue;
                }

                // Build lines one word at a time
                boolean hasWord = false;
                String[] words = StringUtils.split(component, " ");
                for (String word : words) {
                    int length = ChatColor.stripColor(word).length();

                    // If this can not fit on the current line, map a new one
                    if (currentLength != 0 && currentLength + length > maxLength) {
                        // Build new line from current color prefix and builder
                        String newLine = colorPrefix + currentLine;
                        // Record current color prefix to carry it over to the next line
                        colorPrefix = ChatColor.getLastColors(newLine);
                        // Add current line to lore, start new line
                        list.add(newLine);
                        currentLine.setLength(0);
                        currentLine.append(wrapPrefix);
                        currentLength = wrapPrefix.length();
                    } else if (hasWord) {
                        // If we've already added a word, then add a space between
                        currentLine.append(" ");
                    }
                    // Add word to line
                    currentLine.append(word);
                    currentLength += length;
                    hasWord = true;
                }
            }

            // Add anything left over on this line
            if (currentLength > 0) {
                list.add(colorPrefix + currentLine);
            }
        }
    }

    @Override
    public boolean hasItem(Inventory inventory, String itemName) {
        ItemStack itemStack = getItem(inventory, itemName);
        return itemStack != null;
    }

    @Override
    public ItemStack getItem(Inventory inventory, String itemName) {
        if (inventory == null) {
            return null;
        }
        ItemStack[] items = inventory.getContents();
        for (ItemStack item : items) {
            if (item != null && item.hasItemMeta()) {
                String displayName = item.getItemMeta().getDisplayName();
                if (displayName != null && displayName.equals(itemName)) {
                    return item;
                }
            }
        }
        return null;
    }
}
