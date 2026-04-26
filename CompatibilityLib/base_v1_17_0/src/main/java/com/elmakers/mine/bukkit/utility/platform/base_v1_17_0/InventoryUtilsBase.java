package com.elmakers.mine.bukkit.utility.platform.base_v1_17_0;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.utility.Base64Coder;
import com.elmakers.mine.bukkit.utility.ChatUtils;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.CurrencyAmount;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.platform.InventoryUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.google.common.collect.Multimap;

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

    public boolean saveTagsToItem(ConfigurationSection tags, ItemStack item) {
        Object handle = platform.getItemUtils().getHandle(item);
        if (handle == null) return false;
        Object tag = platform.getItemUtils().getOrCreateTag(handle);
        if (tag == null) return false;

        return addTagsToNBT(ConfigUtils.toMap(tags), tag);
    }

    public boolean saveTagsToItem(Map<String, Object> tags, ItemStack item)
    {
        Object handle = platform.getItemUtils().getHandle(item);
        if (handle == null) return false;
        Object tag = platform.getItemUtils().getOrCreateTag(handle);
        if (tag == null) return false;

        return addTagsToNBT(tags, tag);
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
        if (o instanceof Integer) return (long)(Integer)o;
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
        if (o instanceof Long) return (double)(Long)o;
        if (o instanceof Byte) return (double)(Byte)o;
        if (o instanceof String) return Double.parseDouble((String)o);
        return null;
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
                String operation = null;
                UUID uuid = null;
                ConfigurationSection attributeConfiguration = attributeConfig.getConfigurationSection(attributeKey);
                if (attributeConfiguration != null) {
                    attributeKey = attributeConfiguration.getString("attribute", attributeKey);
                    value = attributeConfiguration.getDouble("value");
                    slot = attributeConfiguration.getString("slot", slot);
                    operation = attributeConfiguration.getString("operation");
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
    public int getMapId(ItemStack mapItem) {
        if (platform.isCurrentVersion()) {
            return platform.getNBTUtils().getInt(mapItem, "map", 0);
        }

        return mapItem.getDurability();
    }

    @Override
    public void setMapId(ItemStack mapItem, int id) {
        if (platform.isCurrentVersion()) {
            platform.getNBTUtils().setInt(mapItem, "map", id);
        } else {
            mapItem.setDurability((short)id);
        }
    }

    @Override
    public ItemStack createMap(Material material, int mapId) {
        short durability = platform.isCurrentVersion() ? 0 : (short) mapId;
        ItemStack mapItem = platform.getDeprecatedUtils().createItemStack(material, 1, durability);
        if (platform.isCurrentVersion()) {
            mapItem = platform.getItemUtils().makeReal(mapItem);
            platform.getNBTUtils().setInt(mapItem, "map", mapId);
        }
        return mapItem;
    }

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

    public boolean addTagsToNBT(Map<String, Object> tags, Object node)
    {
        if (node == null) {
            platform.getLogger().warning("Trying to save tags to a null node");
            return false;
        }
        if (!NMSUtils.class_NBTTagCompound.isAssignableFrom(node.getClass())) {
            platform.getLogger().warning("Trying to save tags to a non-CompoundTag");
            return false;
        }

        for (Map.Entry<String, Object> tag : tags.entrySet()) {
            Object value = tag.getValue();
            try {
                Object wrappedTag = wrapInTag(value);
                if (wrappedTag == null) continue;
                NMSUtils.class_NBTTagCompound_setMethod.invoke(node, tag.getKey(), wrappedTag);
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Error saving item data tag " + tag.getKey(), ex);
            }
        }

        return true;
    }

    public boolean saveTagsToNBT(ConfigurationSection tags, Object node) {
        return saveTagsToNBT(tags, node, null);
    }

    public boolean saveTagsToNBT(ConfigurationSection tags, Object node, Set<String> tagNames) {
        return saveTagsToNBT(ConfigUtils.toMap(tags), node, tagNames);
    }

    public boolean saveTagsToNBT(Map<String, Object> tags, Object node, Set<String> tagNames)
    {
        if (node == null) {
            platform.getLogger().warning("Trying to save tags to a null node");
            return false;
        }
        if (!NMSUtils.class_NBTTagCompound.isAssignableFrom(node.getClass())) {
            platform.getLogger().warning("Trying to save tags to a non-CompoundTag");
            return false;
        }

        if (tagNames == null) {
            tagNames = tags.keySet();
        }

        // Remove tags that were not included
        Set<String> currentTags = getTagKeys(node);
        if (currentTags != null && !tagNames.containsAll(currentTags)) {
            // Need to copy this, getKeys returns a live list and bad things can happen.
            currentTags = new HashSet<>(currentTags);
        } else {
            currentTags = null;
        }

        for (String tagName : tagNames)
        {
            if (currentTags != null) currentTags.remove(tagName);
            Object value = tags.get(tagName);
            try {
                Object wrappedTag = wrapInTag(value);
                if (wrappedTag == null) continue;
                NMSUtils.class_NBTTagCompound_setMethod.invoke(node, tagName, wrappedTag);
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Error saving item data tag " + tagName, ex);
            }
        }

        // Finish removing any remaining properties
        if (currentTags != null) {
            for (String currentTag : currentTags) {
                platform.getNBTUtils().removeMeta(node, currentTag);
            }
        }

        return true;
    }

    public Object wrapInTag(Object value)
        throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (value == null) return null;
        Object wrappedValue = null;
        if (value instanceof Boolean) {
            wrappedValue = NMSUtils.class_NBTTagByte_constructor.newInstance((byte)((boolean)value ? 1 : 0));
        } else if (value instanceof Double) {
            wrappedValue = NMSUtils.class_NBTTagDouble_constructor.newInstance(value);
        } else if (value instanceof Float) {
            wrappedValue = NMSUtils.class_NBTTagFloat_constructor.newInstance(value);
        } else if (value instanceof Integer) {
            wrappedValue = NMSUtils.class_NBTTagInt_constructor.newInstance(value);
        } else if (value instanceof Long) {
            wrappedValue = NMSUtils.class_NBTTagLong_constructor.newInstance(value);
        } else if (value instanceof ConfigurationSection) {
            wrappedValue = NMSUtils.class_NBTTagCompound_constructor.newInstance();
            saveTagsToNBT((ConfigurationSection)value, wrappedValue, null);
        } else if (value instanceof Map) {
            wrappedValue = NMSUtils.class_NBTTagCompound_constructor.newInstance();
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>)value;
            addTagsToNBT(valueMap, wrappedValue);
        } else if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> list = (Collection<Object>)value;
            Object listMeta = NMSUtils.class_NBTTagList_constructor.newInstance();
            if (list.size() > 1 && list instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> checkList = (List<Object>)value;
                Object first = checkList.get(0);
                Object second = checkList.get(1);
                if (first instanceof String && !(second instanceof String)) {
                    list = new ArrayList<>();
                    for (int i = 1; i < checkList.size(); i++) {
                        if (first.equals("I")) {
                            list.add(convertToInteger(checkList.get(i)));
                        } else if (first.equals("L")) {
                            list.add(convertToLong(checkList.get(i)));
                        } else if (first.equals("B")) {
                            list.add(convertToByte(checkList.get(i)));
                        } else {
                            list.add(checkList.get(i));
                        }
                    }
                    if (first.equals("B")) {
                        wrappedValue = NMSUtils.class_NBTTagByteArray_constructor.newInstance(makeByteArray((List<Object>)list));
                    } else if (first.equals("I") || NMSUtils.class_NBTTagLongArray_constructor == null) {
                        wrappedValue = NMSUtils.class_NBTTagIntArray_constructor.newInstance(makeIntArray((List<Object>)list));
                    } else if (first.equals("L")) {
                        wrappedValue = NMSUtils.class_NBTTagLongArray_constructor.newInstance(makeLongArray((List<Object>)list));
                    }
                }
            }
            if (wrappedValue == null) {
                for (Object item : list) {
                    if (item != null) {
                        platform.getNBTUtils().addToList(listMeta, wrapInTag(item));
                    }
                }
                wrappedValue = listMeta;
            }
        } else {
            wrappedValue = NMSUtils.class_NBTTagString_consructor.newInstance(value.toString());
        }

        return wrappedValue;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getTagKeys(Object tag) {
        if (tag == null || NMSUtils.class_NBTTagCompound_getKeysMethod == null) {
            return null;
        }

        try {
            return (Set<String>) NMSUtils.class_NBTTagCompound_getKeysMethod.invoke(tag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Object getMetaObject(Object tag, String key) {
        try {
            Object metaBase = NMSUtils.class_NBTTagCompound_getMethod.invoke(tag, key);
            return getTagValue(metaBase);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Object getTagValue(Object tag) throws IllegalAccessException, InvocationTargetException {
        if (tag == null) return null;
        Object value = null;
        if (NMSUtils.class_NBTTagDouble.isAssignableFrom(tag.getClass())) {
            value = NMSUtils.class_NBTTagDouble_dataField.get(tag);
        } else if (NMSUtils.class_NBTTagInt.isAssignableFrom(tag.getClass())) {
            value = NMSUtils.class_NBTTagInt_dataField.get(tag);
        } else if (NMSUtils.class_NBTTagLong.isAssignableFrom(tag.getClass())) {
            value = NMSUtils.class_NBTTagLong_dataField.get(tag);
        } else if (NMSUtils.class_NBTTagFloat.isAssignableFrom(tag.getClass())) {
            value = NMSUtils.class_NBTTagFloat_dataField.get(tag);
        } else if (NMSUtils.class_NBTTagShort.isAssignableFrom(tag.getClass())) {
            value = NMSUtils.class_NBTTagShort_dataField.get(tag);
        } else if (NMSUtils.class_NBTTagByte.isAssignableFrom(tag.getClass())) {
            // This is kind of nasty. Really need a type-juggling container class for config properties.
            value = NMSUtils.class_NBTTagByte_dataField.get(tag);
            if (value != null && value.equals((byte)0)) {
                value = false;
            } else if (value != null && value.equals((byte)1)) {
                value = true;
            }
        } else if (NMSUtils.class_NBTTagList.isAssignableFrom(tag.getClass())) {
            List<?> items = (List<?>) NMSUtils.class_NBTTagList_list.get(tag);
            List<Object> converted = new ArrayList<>();
            for (Object baseTag : items) {
                Object convertedBase = getTagValue(baseTag);
                if (convertedBase != null) {
                    converted.add(convertedBase);
                }
            }
            value = converted;
        } else if (NMSUtils.class_NBTTagString.isAssignableFrom(tag.getClass())) {
            value = NMSUtils.class_NBTTagString_dataField.get(tag);
        } else if (NMSUtils.class_NBTTagCompound.isAssignableFrom(tag.getClass())) {
            Map<String, Object> compoundMap = new HashMap<>();
            Set<String> keys = getTagKeys(tag);
            for (String key : keys) {
                Object baseTag = NMSUtils.class_NBTTagCompound_getMethod.invoke(tag, key);
                Object convertedBase = getTagValue(baseTag);
                if (convertedBase != null) {
                    compoundMap.put(key, convertedBase);
                }
            }
            value = compoundMap;
        }

        return value;
    }

    @Override
    public ItemStack setSkullURL(ItemStack itemStack, URL url, UUID id, String name) {
        try {
            if (platform.getItemUtils().isEmpty(itemStack)) {
                return itemStack;
            }

            Object gameProfile = NMSUtils.class_GameProfile_constructor.newInstance(id, name);
            @SuppressWarnings("unchecked")
            Multimap<String, Object> properties = (Multimap<String, Object>) NMSUtils.class_GameProfile_properties.get(gameProfile);
            if (properties == null) {
                return itemStack;
            }
            itemStack = platform.getItemUtils().makeReal(itemStack);
            if (platform.getItemUtils().isEmpty(itemStack)) {
                return itemStack;
            }

            String textureJSON = "{textures:{SKIN:{url:\"" + url + "\"}}}";
            String encoded = Base64Coder.encodeString(textureJSON);

            properties.put("textures", NMSUtils.class_GameProfileProperty_noSignatureConstructor.newInstance("textures", encoded));

            ItemMeta skullMeta = itemStack.getItemMeta();
            setSkullProfile(skullMeta, gameProfile);

            itemStack.setItemMeta(skullMeta);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return itemStack;
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

    public ItemStack setSkullURLAndName(ItemStack itemStack, URL url, String ownerName, UUID id) {
        try {
            itemStack = platform.getItemUtils().makeReal(itemStack);
            Object skullOwner = platform.getNBTUtils().createTag(itemStack, "SkullOwner");
            platform.getNBTUtils().setString(skullOwner, "Name", ownerName);
            return setSkullURL(itemStack, url, id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return itemStack;
    }

    @Override
    public String getSkullURL(ItemStack skull) {
        return ((SkinUtilsBase)platform.getSkinUtils()).getProfileURL(getSkullProfile(skull.getItemMeta()));
    }

    @Override
    public boolean isSkull(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return NMSUtils.class_CraftMetaSkull.isInstance(meta);
    }

    public Object getSkullProfile(ItemMeta itemMeta)
    {
        Object profile = null;
        try {
            if (itemMeta == null || !NMSUtils.class_CraftMetaSkull.isInstance(itemMeta)) return null;
            profile = NMSUtils.class_CraftMetaSkull_profile.get(itemMeta);
        } catch (Exception ignore) {

        }
        return profile;
    }

    public Object getSkullProfile(Skull state)
    {
        Object profile = null;
        try {
            if (state == null || !NMSUtils.class_CraftSkull.isInstance(state)) return false;
            profile = NMSUtils.class_CraftSkull_profile.get(state);
        } catch (Exception ignore) {

        }
        return profile;
    }

    public boolean setSkullProfile(ItemMeta itemMeta, Object data)
    {
        try {
            if (itemMeta == null || !NMSUtils.class_CraftMetaSkull.isInstance(itemMeta)) return false;
            if (NMSUtils.class_CraftMetaSkull_setProfileMethod != null) {
                NMSUtils.class_CraftMetaSkull_setProfileMethod.invoke(itemMeta, data);
            } else {
                NMSUtils.class_CraftMetaSkull_profile.set(itemMeta, data);
            }
            return true;
        } catch (Exception ignore) {

        }
        return false;
    }

    public boolean setSkullProfile(Skull state, Object data)
    {
        try {
            if (state == null || !NMSUtils.class_CraftSkull.isInstance(state)) return false;
            NMSUtils.class_CraftSkull_profile.set(state, data);
            return true;
        } catch (Exception ignore) {

        }

        return false;
    }
}
