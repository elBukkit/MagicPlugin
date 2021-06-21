package com.elmakers.mine.bukkit.utility.platform.base;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.CurrencyAmount;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public abstract class InventoryUtilsBase implements InventoryUtils {
    protected final Platform platform;

    protected InventoryUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    @Override
    public CurrencyAmount getCurrencyAmount(ItemStack item) {
        if (platform.getItemUtils().isEmpty(item)) return null;

        Object currency = platform.getNBTUtils().getNode(item, "currency");
        if (currency != null) {
            String currencyType = platform.getNBTUtils().getMetaString(currency, "type");
            if (currencyType != null) {
                return new CurrencyAmount(currencyType, platform.getNBTUtils().getMetaInt(currency, "amount"));
            }
            return null;
        }

        // Support for legacy SP items
        int spAmount = platform.getNBTUtils().getMetaInt(item, "sp", 0);
        if (spAmount > 0) {
            return new CurrencyAmount("sp", spAmount);
        }
        return null;
    }

    @Override
    public boolean saveTagsToItem(ConfigurationSection tags, ItemStack item) {
        Object handle = platform.getItemUtils().getHandle(item);
        if (handle == null) return false;
        Object tag = platform.getItemUtils().getTag(handle);
        if (tag == null) return false;

        return addTagsToNBT(platform.getCompatibilityUtils().getMap(tags), tag);
    }

    @Override
    public boolean saveTagsToItem(Map<String, Object> tags, ItemStack item)
    {
        Object handle = platform.getItemUtils().getHandle(item);
        if (handle == null) return false;
        Object tag = platform.getItemUtils().getTag(handle);
        if (tag == null) return false;

        return addTagsToNBT(tags, tag);
    }

    @Override
    public boolean configureSkillItem(ItemStack skillItem, String skillClass, ConfigurationSection skillConfig) {
        if (skillItem == null) return false;
        Object handle = platform.getItemUtils().getHandle(skillItem);
        if (handle == null) return false;
        Object tag = platform.getItemUtils().getTag(handle);
        if (tag == null) return false;

        platform.getNBTUtils().setMetaBoolean(tag, "skill", true);

        Object spellNode = platform.getNBTUtils().getNode(skillItem, "spell");
        if (skillClass != null && spellNode != null) {
            platform.getNBTUtils().setMeta(spellNode, "class", skillClass);
        }
        if (skillConfig == null) {
            return true;
        }

        if (skillConfig.getBoolean("undroppable", false)) {
            platform.getNBTUtils().setMetaBoolean(tag, "undroppable", true);
        }
        if (skillConfig.getBoolean("keep", false)) {
            platform.getNBTUtils().setMetaBoolean(tag, "keep", true);
        }
        boolean quickCast = skillConfig.getBoolean("quick_cast", true);
        if (!quickCast && spellNode != null) {
            platform.getNBTUtils().setMetaBoolean(spellNode, "quick_cast", false);
        }

        return true;
    }

    @Override
    public boolean saveTagsToNBT(ConfigurationSection tags, Object node) {
        return saveTagsToNBT(tags, node, null);
    }

    @Override
    public boolean saveTagsToNBT(ConfigurationSection tags, Object node, Set<String> tagNames) {
        return saveTagsToNBT(platform.getCompatibilityUtils().getMap(tags), node, tagNames);
    }

    protected byte[] makeByteArray(List<Object> list) {
        byte[] a = new byte[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            Byte b = (Byte)list.get(i);
            a[i] = b == null ? 0 : b;
        }

        return a;
    }

    protected int[] makeIntArray(List<Object> list) {
        int[] a = new int[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            Integer value = (Integer)list.get(i);
            a[i] = value == null ? 0 : value;
        }

        return a;
    }

    protected long[]  makeLongArray(List<Object> list) {
        long[] a = new long[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            Long l = (Long)list.get(i);
            a[i] = l == null ? 0 : l;
        }

        return a;
    }

    protected Long convertToLong(Object o) {
        if (o == null) return null;
        if (o instanceof Long) return (Long)o;
        if (o instanceof Integer) return (long)(int)(Integer)o;
        if (o instanceof Byte) return (long)(Byte)o;
        if (o instanceof Double) return (long)(double)(Double)o;
        if (o instanceof String) return Long.parseLong((String)o);
        return null;
    }

    protected Integer convertToInteger(Object o) {
        Long intVal = convertToLong(o);
        return intVal == null ? null : (int)(long)intVal;
    }

    protected Byte convertToByte(Object o) {
        Long intVal = convertToLong(o);
        return intVal == null ? null : (byte)(long)intVal;
    }

    protected Short convertToShort(Object o) {
        Long intVal = convertToLong(o);
        return intVal == null ? null : (short)(long)intVal;
    }

    protected Double convertToDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Double) return (Double)o;
        if (o instanceof Integer) return (double)(Integer)o;
        if (o instanceof Long) return (double)(long)(Long)o;
        if (o instanceof Byte) return (double)(Byte)o;
        if (o instanceof String) return Double.parseDouble((String)o);
        return null;
    }

    @Override
    public ItemStack setSkullURL(ItemStack itemStack, String url) {
        try {
            // Using a fixed non-random UUID here so skulls of the same type can stack
            return setSkullURL(itemStack, new URL(url), CompatibilityConstants.SKULL_UUID);
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
    public ItemStack setSkullURLAndName(ItemStack itemStack, URL url, String ownerName, UUID id) {
        try {
            itemStack = platform.getItemUtils().makeReal(itemStack);
            Object skullOwner = platform.getNBTUtils().createNode(itemStack, "SkullOwner");
            platform.getNBTUtils().setMeta(skullOwner, "Name", ownerName);
            return setSkullURL(itemStack, url, id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return itemStack;
    }

    @Override
    public String getSkullURL(ItemStack skull) {
        return platform.getSkinUtils().getProfileURL(getSkullProfile(skull.getItemMeta()));
    }

    @Override
    public void makeKeep(ItemStack itemStack) {
        platform.getNBTUtils().setMetaBoolean(itemStack, "keep", true);
    }

    @Override
    public boolean isKeep(ItemStack itemStack) {
        return platform.getNBTUtils().hasMeta(itemStack, "keep");
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
                } else {
                    value = attributeConfig.getDouble(attributeKey);
                }
                Attribute attribute = Attribute.valueOf(attributeKey.toUpperCase());
                if (!platform.getCompatibilityUtils().setItemAttribute(item, attribute, value, slot, operation)) {
                    platform.getLogger().warning("Failed to set attribute: " + attributeKey);
                }
            } catch (Exception ex) {
                platform.getLogger().warning("Invalid attribute: " + attributeKey);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void applyEnchantments(ItemStack item, ConfigurationSection enchantConfig) {
        if (item == null) return;

        Set<Enchantment> keep = null;
        if (enchantConfig != null) {
            keep = new HashSet<>();
            CompatibilityUtils compatibilityUtils = platform.getCompatibilityUtils();;
            Collection<String> enchantKeys = enchantConfig.getKeys(false);
            for (String enchantKey : enchantKeys)
            {
                try {
                    Enchantment enchantment = compatibilityUtils.getEnchantmentByKey(enchantKey);
                    item.addUnsafeEnchantment(enchantment, enchantConfig.getInt(enchantKey));
                    keep.add(enchantment);
                } catch (Exception ex) {
                    platform.getLogger().warning("Invalid enchantment: " + enchantKey);
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
    @SuppressWarnings("deprecation")
    public boolean addEnchantments(ItemStack item, ConfigurationSection enchantConfig) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        boolean addedAny = false;
        if (enchantConfig != null) {
            CompatibilityUtils compatibilityUtils = platform.getCompatibilityUtils();;
            Collection<String> enchantKeys = enchantConfig.getKeys(false);
            for (String enchantKey : enchantKeys) {
                try {
                    Enchantment enchantment = compatibilityUtils.getEnchantmentByKey(enchantKey);
                    int level = enchantConfig.getInt(enchantKey);
                    if (meta.hasConflictingEnchant(enchantment)) continue;
                    if (meta.getEnchantLevel(enchantment) >= level) continue;
                    if (meta.addEnchant(enchantment, level, false)) {
                        addedAny = true;
                    }
                } catch (Exception ex) {
                    platform.getLogger().warning("Invalid enchantment: " + enchantKey);
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
        if (platform.isCurrentVersion()) {
            return platform.getNBTUtils().getMetaInt(mapItem, "map", 0);
        }

        return mapItem.getDurability();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setMapId(ItemStack mapItem, int id) {
        if (platform.isCurrentVersion()) {
            platform.getNBTUtils().setMetaInt(mapItem, "map", id);
        } else {
            mapItem.setDurability((short)id);
        }
    }

    @Override
    public void convertIntegers(Map<String, Object> m) {
        for (Map.Entry<String, Object> entry : m.entrySet()) {
            Object value = entry.getValue();
            if (value != null && value instanceof Double) {
                double d = (Double) value;
                if (d == (int)d) {
                    entry.setValue((int)d);
                }
            } else if (value != null && value instanceof Float) {
                float f = (Float) value;
                if (f == (int)f) {
                    entry.setValue((int)f);
                }
            } else if (value != null && value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>)value;
                convertIntegers(map);
            }
        }
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
        String colorPrefix = "";
        String[] lines = StringUtils.split(text, "\n\r");
        for (String line : lines) {
            line = prefix + line;
            while (line.length() > maxLength)
            {
                int spaceIndex = line.lastIndexOf(' ', maxLength);
                if (spaceIndex <= 0) {
                    list.add(colorPrefix + line);
                    return;
                }
                String colorText = colorPrefix + line.substring(0, spaceIndex);
                colorPrefix = ChatColor.getLastColors(colorText);
                list.add(colorText);
                line = line.substring(spaceIndex);
            }

            list.add(colorPrefix + line);
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
