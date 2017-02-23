package com.elmakers.mine.bukkit.utility;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.google.common.collect.Multimap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
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
    public static boolean saveTagsToItem(ConfigurationSection tags, ItemStack item)
    {
        Object handle = getHandle(item);
        if (handle == null) return false;
        Object tag = getTag(handle);
        if (tag == null) return false;
        
        return saveTagsToNBT(tags, tag, null);
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
            wrappedValue = class_NBTTagDouble_constructor.newInstance((double)value);
        } else if (value instanceof Float) {
            wrappedValue = class_NBTTagFloat_constructor.newInstance((float)value);
        } else if (value instanceof Integer) {
            wrappedValue = class_NBTTagInt_constructor.newInstance((int)value);
        } else if (value instanceof Long) {
            wrappedValue = class_NBTTagLong_constructor.newInstance((long)value);
        } else if (value instanceof ConfigurationSection) {
            wrappedValue = class_NBTTagCompound.newInstance();
            saveTagsToNBT((ConfigurationSection)value, wrappedValue, null);
        } else if (value instanceof Map) {
            wrappedValue = class_NBTTagCompound.newInstance();
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>)value;
            saveTagsToNBT(valueMap, wrappedValue, null);
        } else if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> list = (Collection<Object>)value;
            Object listMeta = class_NBTTagList.newInstance();
            for (Object item : list) {
                if (item != null) {
                    class_NBTTagList_addMethod.invoke(listMeta, wrapInTag(item));
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

    public static boolean loadAllTagsFromNBT(ConfigurationSection tags, Object tag)
    {
        try {
            Set<String> keys = getTagKeys(tag);
            if (keys == null) return false;

            for (String tagName : keys) {
                Object metaBase = class_NBTTagCompound_getMethod.invoke(tag, tagName);
                if (metaBase != null) {
                    if (class_NBTTagCompound.isAssignableFrom(metaBase.getClass())) {
                        ConfigurationSection newSection = tags.createSection(tagName);
                        loadAllTagsFromNBT(newSection, metaBase);
                    } else if (class_NBTTagString.isAssignableFrom(metaBase.getClass())) {
                        // Special conversion case here... not sure if this is still a good idea
                        // But there would be downstream effects.
                        // TODO: Look closer.
                        ConfigurationUtils.set(tags, tagName, class_NBTTagString_dataField.get(metaBase));
                    } else {
                        tags.set(tagName, getTagValue(metaBase));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object getTagValue(Object tag) throws IllegalAccessException, InvocationTargetException {
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
            List items = (List)class_NBTTagList_list.get(tag);
            List<Object> converted = new ArrayList<Object>();
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

    public static boolean loadAllTagsFromNBT(ConfigurationSection tags, ItemStack item)
    {
        if (item == null) {
            return false;
        }
        Object handle = getHandle(item);
        if (handle == null) return false;
        Object tag = getTag(handle);
        if (tag == null) return false;
        
        return loadAllTagsFromNBT(tags, tag);
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

    public static ItemStack getURLSkull(String url) {
        // The "MHF_Question" is here so serialization doesn't cause an NPE
        return getURLSkull(url, "MHF_Question", UUID.randomUUID(), null);
    }

    public static ItemStack getURLSkull(URL url) {
        // The "MHF_Question" is here so serialization doesn't cause an NPE
        return getURLSkull(url, "MHF_Question", UUID.randomUUID(), null);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getURLSkull(String url, String ownerName, UUID id, String itemName) {
        try {
            return getURLSkull(new URL(url), ownerName, id, itemName);
        } catch (MalformedURLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Malformed URL: " + url, e);
        }
        return new ItemStack(Material.SKULL_ITEM, 1, (short)0, (byte)3);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getURLSkull(URL url, String ownerName, UUID id, String itemName) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)0, (byte)3);
        if (itemName != null) {
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName(itemName);
            skull.setItemMeta(meta);
        }

        try {
            skull = makeReal(skull);
            Object skullOwner = createNode(skull, "SkullOwner");
            setMeta(skullOwner, "Id", id.toString());
            setMeta(skullOwner, "Name", ownerName);
            Object properties = createNode(skullOwner, "Properties");

            Object listMeta = class_NBTTagList.newInstance();
            Object textureNode = class_NBTTagCompound.newInstance();

            String textureJSON = "{textures:{SKIN:{url:\"" + url + "\"}}}";
            String encoded = Base64Coder.encodeString(textureJSON);

            setMeta(textureNode, "Value", encoded);
            class_NBTTagList_addMethod.invoke(listMeta, textureNode);
            class_NBTTagCompound_setMethod.invoke(properties, "textures", listMeta);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return skull;
    }

    public static String getProfileURL(Object profile)
    {
        String url = null;
        if (profile == null) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Multimap<String, Object> properties = (Multimap<String, Object>)class_GameProfile_properties.get(profile);
            Collection<Object> textures = properties.get("textures");
            if (textures != null && textures.size() > 0)
            {
                Object textureProperty = textures.iterator().next();
                String texture = (String)class_GameProfileProperty_value.get(textureProperty);
                String decoded = Base64Coder.decodeString(texture);
                url = decoded.replace("{textures:{SKIN:{url:\"", "").replace("\"}}}", "").trim();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return url;
    }

    public static String getSkullURL(ItemStack skull) {
        return getProfileURL(getSkullProfile(skull.getItemMeta()));
    }

    public static ItemStack getPlayerSkull(String playerName)
    {
        return getPlayerSkull(playerName, UUID.randomUUID(), null);
    }

    public static ItemStack getPlayerSkull(String playerName, String itemName)
    {
        return getPlayerSkull(playerName, UUID.randomUUID(), itemName);
    }

    public static ItemStack getPlayerSkull(String playerName, UUID uuid)
    {
        return getPlayerSkull(playerName, uuid, null);
    }

    public static ItemStack getPlayerSkull(String playerName, UUID uuid, String itemName)
    {
        return getURLSkull(getPlayerSkullURL(playerName), playerName, uuid, itemName);
    }
    
    public static String getPlayerSkullURL(String playerName)
    {
        return "http://skins.minecraft.net/MinecraftSkins/" + playerName + ".png";
    }

    public static ItemStack getPlayerSkull(Player player)
    {
        return getPlayerSkull(player, null);
    }

    public static ItemStack getPlayerSkull(Player player, String itemName)
    {
        return getPlayerSkull(player.getName(), player.getUniqueId(), itemName);
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

    public static void wrapText(String text, int maxLength, Collection<String> list)
    {
        wrapText("", text, maxLength, list);
    }

    public static void wrapText(String prefix, String text, int maxLength, Collection<String> list)
    {
        while (text.length() > maxLength)
        {
            int spaceIndex = text.lastIndexOf(' ', maxLength);
            if (spaceIndex <= 0) {
                list.add(prefix + text);
                return;
            }
            list.add(prefix + text.substring(0, spaceIndex));
            text = text.substring(spaceIndex);
        }

        list.add(prefix + text);
    }

    public static boolean hasItem(Mage mage, String itemName) {
        return hasItem(mage.getInventory(), itemName);
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
        setMeta(itemStack, "keep", "true");
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
    
    public static boolean isEmpty(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return true;
        if (class_ItemStack_isEmptyMethod == null) return false;
        try {
            Object handle = getHandle(itemStack);
            if (handle == null) return false;
           return (Boolean)class_ItemStack_isEmptyMethod.invoke(handle);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
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
            String full = "{";
            boolean first = true;
            for (String key : keys) {
                if (!first) {
                    full += ",";
                }
                first = false;
                full += key + "=" + describeProperty(section.get(key));
            }
            propertyString = full + "}";
        } else {
            propertyString = property.toString();
        }
        if (maxLength > 0 && propertyString.length() > maxLength - 3) {
            propertyString = propertyString.substring(0, maxLength - 3) + "...";
        }
        return propertyString;
    }
}