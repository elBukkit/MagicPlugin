package com.elmakers.mine.bukkit.utility;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class InventoryUtils extends NMSUtils
{
    public static int MAX_LORE_LENGTH = 24;
    public static int MAX_PROPERTY_DISPLAY_LENGTH = 50;

    public static boolean saveTagsToItem(ConfigurationSection tags, ItemStack item)
    {
        Object handle = getHandle(item);
        if (handle == null) return false;
        Object tag = getTag(handle);
        if (tag == null) return false;
        
        return saveTagsToNBT(tags, tag, null);
    }

    public static boolean configureSkillItem(ItemStack skillItem, String skillClass, ConfigurationSection skillConfig) {
        if (skillItem == null) return false;
        Object handle = getHandle(skillItem);
        if (handle == null) return false;
        Object tag = getTag(handle);
        if (tag == null) return false;

        setMetaBoolean(tag, "skill", true);

        if (skillConfig == null) {
            return true;
        }

        if (skillConfig.getBoolean("undroppable", false)) {
            setMetaBoolean(tag, "undroppable", true);
        }
        if (skillConfig.getBoolean("keep", false)) {
            setMetaBoolean(tag, "keep", true);
        }
        boolean quickCast = skillConfig.getBoolean("quick_cast", true);
        if (skillClass != null || !quickCast) {
            Object spellNode = InventoryUtils.getNode(skillItem, "spell");
            if (spellNode != null) {
                if (skillClass != null) {
                    InventoryUtils.setMeta(spellNode, "class", skillClass);
                }
                if (!quickCast) {
                    InventoryUtils.setMetaBoolean(spellNode, "quick_cast", false);
                }
            }
        }

        return true;
    }

    public static boolean saveTagsToNBT(ConfigurationSection tags, Object node)
    {
        return saveTagsToNBT(tags, node, null);
    }

    public static boolean saveTagsToNBT(ConfigurationSection tags, Object node, Set<String> tagNames)
    {
        return saveTagsToNBT(getMap(tags), node, tagNames);
    }
    
    public static boolean saveTagsToNBT(Map<String, Object> tags, Object node, Set<String> tagNames)
    {
        if (node == null) {
            Bukkit.getLogger().warning("Trying to save tags to a null node");
            return false;
        }
        if (!class_NBTTagCompound.isAssignableFrom(node.getClass())) {
            Bukkit.getLogger().warning("Trying to save tags to a non-CompoundTag");
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
                class_NBTTagCompound_setMethod.invoke(node, tagName, wrappedTag);
            } catch (Exception ex) {
                org.bukkit.Bukkit.getLogger().log(Level.WARNING, "Error saving item data tag " + tagName, ex);
            }
        }

        // Finish removing any remaining properties
        if (currentTags != null) {
            for (String currentTag : currentTags) {
                removeMeta(node, currentTag);
            }
        }

        return true;
    }

    public static Object wrapInTag(Object value)
        throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (value == null) return null;
        Object wrappedValue = null;
        if (value instanceof Boolean) {
            wrappedValue = class_NBTTagByte_constructor.newInstance((byte)((boolean)value ? 1 : 0));
        } else if (value instanceof Double) {
            wrappedValue = class_NBTTagDouble_constructor.newInstance((Double)value);
        } else if (value instanceof Float) {
            wrappedValue = class_NBTTagFloat_constructor.newInstance((Float)value);
        } else if (value instanceof Integer) {
            wrappedValue = class_NBTTagInt_constructor.newInstance((Integer)value);
        } else if (value instanceof Long) {
            wrappedValue = class_NBTTagLong_constructor.newInstance((Long)value);
        } else if (value instanceof ConfigurationSection) {
            wrappedValue = class_NBTTagCompound_constructor.newInstance();
            saveTagsToNBT((ConfigurationSection)value, wrappedValue, null);
        } else if (value instanceof Map) {
            wrappedValue = class_NBTTagCompound_constructor.newInstance();
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>)value;
            saveTagsToNBT(valueMap, wrappedValue, null);
        } else if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> list = (Collection<Object>)value;
            Object listMeta = class_NBTTagList_constructor.newInstance();
            for (Object item : list) {
                if (item != null) {
                    addToList(listMeta, wrapInTag(item));
                }
            }
            wrappedValue = listMeta;
        } else {
            wrappedValue = class_NBTTagString_consructor.newInstance(value.toString());
        }

        return wrappedValue;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getTagKeys(Object tag) {
        if (tag == null || class_NBTTagCompound_getKeysMethod == null) {
            return null;
        }

        try {
            return (Set<String>) class_NBTTagCompound_getKeysMethod.invoke(tag);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object getMetaObject(Object tag, String key) {
        try {
            Object metaBase = class_NBTTagCompound_getMethod.invoke(tag, key);
            return getTagValue(metaBase);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Object getTagValue(Object tag) throws IllegalAccessException, InvocationTargetException {
        if (tag == null) return null;
        Object value = null;
        if (class_NBTTagDouble.isAssignableFrom(tag.getClass())) {
            value = class_NBTTagDouble_dataField.get(tag);
        } else if (class_NBTTagInt.isAssignableFrom(tag.getClass())) {
            value = class_NBTTagInt_dataField.get(tag);
        } else if (class_NBTTagLong.isAssignableFrom(tag.getClass())) {
            value = class_NBTTagLong_dataField.get(tag);
        } else if (class_NBTTagFloat.isAssignableFrom(tag.getClass())) {
            value = class_NBTTagFloat_dataField.get(tag);
        } else if (class_NBTTagShort.isAssignableFrom(tag.getClass())) {
            value = class_NBTTagShort_dataField.get(tag);
        } else if (class_NBTTagByte.isAssignableFrom(tag.getClass())) {
            // This is kind of nasty. Really need a type-juggling container class for config properties.
            value = class_NBTTagByte_dataField.get(tag);
            if (value != null && value.equals((byte)0)) {
                value = false;
            } else if (value != null && value.equals((byte)1)) {
                value = true;
            }
        } else if (class_NBTTagList.isAssignableFrom(tag.getClass())) {
            List<?> items = (List<?>)class_NBTTagList_list.get(tag);
            List<Object> converted = new ArrayList<>();
            for (Object baseTag : items) {
                Object convertedBase = getTagValue(baseTag);
                if (convertedBase != null) {
                    converted.add(convertedBase);
                }
            }
            value = converted;
        } else if (class_NBTTagString.isAssignableFrom(tag.getClass())) {
            value = class_NBTTagString_dataField.get(tag);
        } else if (class_NBTTagCompound.isAssignableFrom(tag.getClass())) {
            Map<String, Object> compoundMap = new HashMap<>();
            Set<String> keys = getTagKeys(tag);
            for (String key : keys) {
                Object baseTag = class_NBTTagCompound_getMethod.invoke(tag, key);
                Object convertedBase = getTagValue(baseTag);
                if (convertedBase != null) {
                    compoundMap.put(key, convertedBase);
                }
            }
            value = compoundMap;
        }

        return value;
    }

    public static boolean inventorySetItem(Inventory inventory, int index, ItemStack item) {
        try {
            Method setItemMethod = class_CraftInventoryCustom.getMethod("setItem", Integer.TYPE, ItemStack.class);
            setItemMethod.invoke(inventory, index, item);
            return true;
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean setInventoryResults(Inventory inventory, ItemStack item) {
        try {
            Method getResultsMethod = inventory.getClass().getMethod("getResultInventory");
            Object inv = getResultsMethod.invoke(inventory);
            Method setItemMethod = inv.getClass().getMethod("setItem", Integer.TYPE, class_ItemStack);
            setItemMethod.invoke(inv, 0, getHandle(item));
            return true;
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void setNewSkullURL(ItemStack itemStack, String url) {
        try {
            setSkullURL(itemStack, new URL(url), UUID.randomUUID());
        } catch (MalformedURLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Malformed URL: " + url, e);
        }
    }

    public static ItemStack setSkullURL(ItemStack itemStack, String url) {
        try {
            return setSkullURLAndName(itemStack, new URL(url), "MHF_Question", UUID.randomUUID());
        } catch (MalformedURLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Malformed URL: " + url, e);
        }
        return itemStack;
    }

    public static ItemStack setSkullURLAndName(ItemStack itemStack, URL url, String ownerName, UUID id) {
        try {
            itemStack = makeReal(itemStack);
            Object skullOwner = createNode(itemStack, "SkullOwner");
            setMeta(skullOwner, "Name", ownerName);
            setSkullURL(itemStack, url, id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return itemStack;
    }

    public static void setSkullURL(ItemStack itemStack, URL url, UUID id) {
        try {
            Object skullOwner = createNode(itemStack, "SkullOwner");
            setMeta(skullOwner, "Id", id.toString());

            Object properties = createNode(skullOwner, "Properties");

            Object listMeta = class_NBTTagList_constructor.newInstance();
            Object textureNode = class_NBTTagCompound_constructor.newInstance();

            String textureJSON = "{textures:{SKIN:{url:\"" + url + "\"}}}";
            String encoded = Base64Coder.encodeString(textureJSON);

            setMeta(textureNode, "Value", encoded);
            addToList(listMeta, textureNode);
            class_NBTTagCompound_setMethod.invoke(properties, "textures", listMeta);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getSkullURL(ItemStack skull) {
        return SkinUtils.getProfileURL(getSkullProfile(skull.getItemMeta()));
    }
    
    @Deprecated
    public static String getPlayerSkullURL(String playerName)
    {
        return SkinUtils.getOnlineSkinURL(playerName);
    }

    public static Object getSkullProfile(ItemMeta itemMeta)
    {
        Object profile = null;
        try {
            if (itemMeta == null || !class_CraftMetaSkull.isInstance(itemMeta)) return null;
            profile = class_CraftMetaSkull_profile.get(itemMeta);
        } catch (Exception ex) {

        }
        return profile;
    }

    public static boolean setSkullProfile(ItemMeta itemMeta, Object data)
    {
        try {
            if (itemMeta == null || !class_CraftMetaSkull.isInstance(itemMeta)) return false;
            class_CraftMetaSkull_profile.set(itemMeta, data);
            return true;
        } catch (Exception ex) {

        }
        return false;
    }

    public static Object getSkullProfile(Skull state)
    {
        Object profile = null;
        try {
            if (state == null || !class_CraftSkull.isInstance(state)) return false;
            profile = class_CraftSkull_profile.get(state);
        } catch (Exception ex) {

        }
        return profile;
    }

    public static boolean setSkullProfile(Skull state, Object data)
    {
        try {
            if (state == null || !class_CraftSkull.isInstance(state)) return false;
            class_CraftSkull_profile.set(state, data);
            return true;
        } catch (Exception ex) {

        }

        return false;
    }

    public static void wrapText(String text, Collection<String> list)
    {
        wrapText(text, MAX_LORE_LENGTH, list);
    }

    public static void wrapText(String text, String prefix, Collection<String> list)
    {
        wrapText(text, prefix, MAX_LORE_LENGTH, list);
    }

    public static void wrapText(String text, int maxLength, Collection<String> list)
    {
        wrapText(text, "", maxLength, list);
    }

    public static void wrapText(String text, String prefix, int maxLength, Collection<String> list)
    {
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

    public static boolean hasItem(Inventory inventory, String itemName) {
        if (inventory == null) {
            return false;
        }
        ItemStack[] items = inventory.getContents();
        for (ItemStack item : items) {
            if (item != null && item.hasItemMeta()) {
                String displayName = item.getItemMeta().getDisplayName();
                if (displayName != null && displayName.equals(itemName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void openSign(Player player, Location signBlock) {
        try {
            Object tileEntity = getTileEntity(signBlock);
            Object playerHandle = getHandle(player);
            if (tileEntity != null && playerHandle != null) {
                class_EntityPlayer_openSignMethod.invoke(playerHandle, tileEntity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void makeKeep(ItemStack itemStack) {
        setMetaBoolean(itemStack, "keep", true);
    }

    public static boolean isKeep(ItemStack itemStack) {
        return hasMeta(itemStack, "keep");
    }
    
    public static void applyAttributes(ItemStack item, ConfigurationSection attributeConfig, String slot) {
        if (item == null || attributeConfig == null) return;
        Collection<String> attributeKeys = attributeConfig.getKeys(false);
        for (String attributeKey : attributeKeys)
        {
            try {
                Attribute attribute = Attribute.valueOf(attributeKey.toUpperCase());
                double value = attributeConfig.getDouble(attributeKey);
                if (!CompatibilityUtils.setItemAttribute(item, attribute, value, slot)) {
                    Bukkit.getLogger().warning("Failed to set attribute: " + attributeKey);
                }
            } catch (Exception ex) {
                Bukkit.getLogger().warning("Invalid attribute: " + attributeKey);
            }
        }
    }
    
    public static void applyEnchantments(ItemStack item, ConfigurationSection enchantConfig) {
        if (item == null || enchantConfig == null) return;
        Collection<String> enchantKeys = enchantConfig.getKeys(false);
        for (String enchantKey : enchantKeys)
        {
            try {
                Enchantment enchantment = Enchantment.getByName(enchantKey.toUpperCase());
                item.addUnsafeEnchantment(enchantment, enchantConfig.getInt(enchantKey));
            } catch (Exception ex) {
                Bukkit.getLogger().warning("Invalid enchantment: " + enchantKey);
            }
        }
    }

    public static String describeProperty(Object property) {
        return describeProperty(property, 0);
    }

    public static String describeProperty(Object property, int maxLength) {
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

    @SuppressWarnings("EqualsReference")
    public static boolean isSameInstance(ItemStack one, ItemStack two) {
        return one == two;
    }

    public static int getMapId(ItemStack mapItem) {
        if (isCurrentVersion()) {
            return getMetaInt(mapItem, "map", 0);
        }

        return mapItem.getDurability();
    }

    public static void setMapId(ItemStack mapItem, int id) {
        if (isCurrentVersion()) {
            setMetaInt(mapItem, "map", id);
        } else {
            mapItem.setDurability((short)id);
        }
    }
}