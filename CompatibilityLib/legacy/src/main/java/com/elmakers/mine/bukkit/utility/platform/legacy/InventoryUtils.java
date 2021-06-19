package com.elmakers.mine.bukkit.utility.platform.legacy;

import java.lang.reflect.InvocationTargetException;
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

import org.bukkit.Location;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.utility.Base64Coder;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.InventoryUtilsBase;
import com.google.common.collect.Multimap;

@SuppressWarnings("deprecation")
public class InventoryUtils extends InventoryUtilsBase {
    public InventoryUtils(Platform platform) {
        super(platform);
    }

    @Override
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

    @Override
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

    @Override
    public Object wrapInTag(Object value)
        throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (value == null) return null;
        Object wrappedValue = null;
        if (value instanceof Boolean) {
            wrappedValue = NMSUtils.class_NBTTagByte_constructor.newInstance((byte)((boolean)value ? 1 : 0));
        } else if (value instanceof Double) {
            wrappedValue = NMSUtils.class_NBTTagDouble_constructor.newInstance((Double)value);
        } else if (value instanceof Float) {
            wrappedValue = NMSUtils.class_NBTTagFloat_constructor.newInstance((Float)value);
        } else if (value instanceof Integer) {
            wrappedValue = NMSUtils.class_NBTTagInt_constructor.newInstance((Integer)value);
        } else if (value instanceof Long) {
            wrappedValue = NMSUtils.class_NBTTagLong_constructor.newInstance((Long)value);
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

    @Override
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

    @Override
    public Object getMetaObject(Object tag, String key) {
        try {
            Object metaBase = NMSUtils.class_NBTTagCompound_getMethod.invoke(tag, key);
            return getTagValue(metaBase);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
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
    public boolean isSkull(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return NMSUtils.class_CraftMetaSkull.isInstance(meta);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void openSign(Player player, Location signBlock) {
        try {
            Object tileEntity = platform.getCompatibilityUtils().getTileEntity(signBlock);
            Object playerHandle = NMSUtils.getHandle(player);
            if (tileEntity != null && playerHandle != null) {
                NMSUtils.class_EntityPlayer_openSignMethod.invoke(playerHandle, tileEntity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
