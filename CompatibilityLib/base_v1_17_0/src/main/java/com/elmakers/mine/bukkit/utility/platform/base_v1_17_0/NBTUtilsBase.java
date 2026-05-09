package com.elmakers.mine.bukkit.utility.platform.base_v1_17_0;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public abstract class NBTUtilsBase implements NBTUtils {
    protected final Platform platform;

    protected NBTUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    @Override
    public boolean containsTag(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return false;
        return getTag(stack, tag) != null;
    }

    @Override
    public short getShort(Object node, String tag, short defaultValue) {
        Short meta = getOptionalShort(node, tag);
        return meta == null ? defaultValue : meta;
    }

    @Override
    public int getInt(Object node, String tag, int defaultValue) {
        Integer meta = getOptionalInt(node, tag);
        return meta == null ? defaultValue : meta;
    }

    @Override
    public int getInt(ItemStack stack, String tag, int defaultValue) {
        if (platform.getItemUtils().isEmpty(stack)) return defaultValue;
        int result = defaultValue;
        try {
            Object tagObject = platform.getItemUtils().getTag(stack);
            if (tagObject == null) return defaultValue;
            Integer value = getOptionalInt(tagObject, tag);
            result = value == null ? defaultValue : value;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public void setInt(ItemStack stack, String tag, int value) {
        if (platform.getItemUtils().isEmpty(stack)) return;
        try {
            Object craft = platform.getItemUtils().getHandle(stack);
            if (craft == null) return;
            Object tagObject = platform.getItemUtils().getOrCreateTag(craft);
            if (tagObject == null) return;
            setInt(tagObject, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeMeta(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return;

        try {
            Object craft = platform.getItemUtils().getHandle(stack);
            if (craft == null) return;
            Object tagObject = platform.getItemUtils().getTag(craft);
            if (tagObject == null) return;
            removeMeta(tagObject, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void parseAndSet(Object node, String tag, String value) {
        if (value == null) {
            removeMeta(node, tag);
            return;
        }

        boolean isTrue = value.equals("true");
        boolean isFalse = value.equals("false");
        if (isTrue || isFalse) {
            setBoolean(node, tag, isTrue);
        } else {
            try {
                Integer i = Integer.parseInt(value);
                setInt(node, tag, i);
            } catch (Exception ex) {
                try {
                    Double d = Double.parseDouble(value);
                    setDouble(node, tag, d);
                } catch (Exception ex2) {
                    setString(node, tag, value);
                }
            }
        }
    }

    @Override
    public void setBoolean(ItemStack stack, String tag, boolean value) {
        if (platform.getItemUtils().isEmpty(stack)) return;
        try {
            Object craft = platform.getItemUtils().getHandle(stack);
            if (craft == null) return;
            Object tagObject = platform.getItemUtils().getOrCreateTag(craft);
            if (tagObject == null) return;
            setBoolean(tagObject, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean getBoolean(ItemStack stack, String tag, boolean defaultValue) {
        if (platform.getItemUtils().isEmpty(stack)) return defaultValue;
        boolean result = defaultValue;
        try {
            Object tagObject = platform.getItemUtils().getTag(stack);
            if (tagObject == null) return defaultValue;
            Boolean value = getOptionalBoolean(tagObject, tag);
            result = value == null ? defaultValue : value;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean setSpawnEggEntityData(ItemStack spawnEgg, EntityType entityType, Object entityData) {
        return setTag(spawnEgg, "EntityTag", entityData);
    }

    @Override
    public Object getSpawnEggEntityData(ItemStack spawnEgg) {
        return getTag(spawnEgg, "EntityTag");
    }

    @Override
    public org.bukkit.entity.EntityType getSpawnEggEntityType(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack == null ? null : itemStack.getItemMeta();
        if (itemMeta == null || !(itemMeta instanceof SpawnEggMeta)) return null;
        SpawnEggMeta spawnEgg = (SpawnEggMeta) itemMeta;
        return spawnEgg.getSpawnedType();
    }

    @Override
    public void removeSpawnEggEntityData(ItemStack spawnEgg) {
        setSpawnEggEntityData(spawnEgg, null, null);
    }

    @Override
    public String getString(ItemStack stack, String tag, String defaultValue) {
        String result = getString(stack, tag);
        return result == null ? defaultValue : result;
    }

    @Override
    public String getString(Object node, String tag, String defaultValue) {
        String meta = getString(node, tag);
        return meta == null || meta.length() == 0 ? defaultValue : meta;
    }

    @Override
    public void setIntArray(Object tag, String key, int[] value) {
        // Not in legacy versions
    }

    @Override
    public void setByteArray(Object tag, String key, byte[] value) {
        // Not in legacy versions
    }

    @Override
    public void setEmptyList(Object tag, String key) {
        // Not in legacy versions
    }

    @Override
    public boolean writeTagToStream(Object tag, OutputStream output) {
        // Not in legacy versions
        return false;
    }

    @Override
    public boolean saveTagsToItem(ConfigurationSection tags, ItemStack item) {
        return ((InventoryUtilsBase)platform.getInventoryUtils()).saveTagsToItem(tags, item);
    }

    @Override
    public boolean saveTagsToNBT(ConfigurationSection tags, Object node) {
        return ((InventoryUtilsBase)platform.getInventoryUtils()).saveTagsToNBT(tags, node);
    }

    @Override
    public boolean saveTagsToNBT(ConfigurationSection tags, Object node, Set<String> tagNames) {
        return ((InventoryUtilsBase)platform.getInventoryUtils()).saveTagsToNBT(tags, node, tagNames);
    }

    @Override
    public boolean saveTagsToNBT(Map<String, Object> tags, Object node, Set<String> tagNames) {
        return ((InventoryUtilsBase)platform.getInventoryUtils()).saveTagsToNBT(tags, node, tagNames);
    }

    @Override
    public boolean addTagsToNBT(Map<String, Object> tags, Object node) {
        return ((InventoryUtilsBase)platform.getInventoryUtils()).addTagsToNBT(tags, node);
    }

    @Override
    public Object wrapInTag(Object value) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return ((InventoryUtilsBase)platform.getInventoryUtils()).wrapInTag(value);
    }

    @Override
    public Set<String> getTagKeys(Object tag) {
        return ((InventoryUtilsBase)platform.getInventoryUtils()).getTagKeys(tag);
    }

    @Override
    public Object getMetaObject(Object tag, String key) {
        return ((InventoryUtilsBase)platform.getInventoryUtils()).getMetaObject(tag, key);
    }

    @Override
    public Object getTagValue(Object tag) throws IllegalAccessException, InvocationTargetException {
        return ((InventoryUtilsBase)platform.getInventoryUtils()).getTagValue(tag);
    }

    @Override
    public void convertIntegers(Map<String, Object> map) {
        ((InventoryUtilsBase)platform.getInventoryUtils()).convertIntegers(map);
    }

    @Override
    public Object getCompoundTagFromCustomData(Object customData) {
        return null;
    }
}
