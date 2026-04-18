package com.elmakers.mine.bukkit.utility.platform.base;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.platform.NBTUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

public class NBTUtilsBase implements NBTUtils {
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
    public Object getTag(ItemStack stack, String tag) {
        return null;
    }

    @Override
    public Object getTag(Object nbtBase, String tag) {
        return null;
    }

    @Override
    public Object getCompoundTagFromCustomData(Object customData) {
        return null;
    }

    @Override
    public Object createTag(Object nbtBase, String tag) {
        return null;
    }

    @Override
    public Object createTag(ItemStack stack, String tag) {
        return null;
    }

    @Override
    public boolean contains(Object nbtBase, String tag) {
        return false;
    }

    @Override
    public Byte getOptionalByte(Object node, String tag) {
        return 0;
    }

    @Override
    public Short getOptionalShort(Object node, String tag) {
        return 0;
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
    public String getString(Object node, String tag) {
        return "";
    }

    @Override
    public String getString(ItemStack stack, String tag) {
        return "";
    }

    @Override
    public short getShort(Object node, String tag, short defaultValue) {
        Short meta = getOptionalShort(node, tag);
        return meta == null ? defaultValue : meta;
    }

    @Override
    public Integer getOptionalInt(Object node, String tag) {
        return 0;
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
    public Double getOptionalDouble(Object node, String tag) {
        return 0.0;
    }

    @Override
    public void setInt(Object node, String tag, int value) {

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
    public void setDouble(Object node, String tag, double value) {

    }

    @Override
    public void setMetaShort(Object node, String tag, short value) {

    }

    @Override
    public void setIntArray(Object tag, String key, int[] value) {

    }

    @Override
    public void setByteArray(Object tag, String key, byte[] value) {

    }

    @Override
    public void setEmptyList(Object tag, String key) {

    }

    @Override
    public void removeMeta(Object node, String tag) {

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
    public void addToList(Object listObject, Object node) {

    }

    @Override
    public Object readTagFromStream(InputStream input) {
        return null;
    }

    @Override
    public boolean writeTagToStream(Object tag, OutputStream output) {
        return false;
    }

    @Override
    public Set<String> getAllKeys(Object tag) {
        return Set.of();
    }

    @Override
    public Collection<Object> getTagList(Object tag, String key) {
        return List.of();
    }

    @Override
    public Object newCompoundTag() {
        return null;
    }

    @Override
    public boolean setSpawnEggEntityData(ItemStack spawnEgg, Entity entity, Object entityData) {
        return false;
    }

    public boolean setSpawnEggEntityData(ItemStack spawnEgg, Object entityData) {
        return setTag(spawnEgg, "EntityTag", entityData);
    }

    @Override
    public boolean saveTagsToItem(ConfigurationSection tags, ItemStack item) {
        return false;
    }

    @Override
    public boolean saveTagsToNBT(ConfigurationSection tags, Object node) {
        return false;
    }

    @Override
    public boolean saveTagsToNBT(ConfigurationSection tags, Object node, Set<String> tagNames) {
        return false;
    }

    @Override
    public boolean saveTagsToNBT(Map<String, Object> tags, Object node, Set<String> tagNames) {
        return false;
    }

    @Override
    public boolean addTagsToNBT(Map<String, Object> tags, Object node) {
        return false;
    }

    @Override
    public Object wrapInTag(Object value) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return null;
    }

    @Override
    public Set<String> getTagKeys(Object tag) {
        return Set.of();
    }

    @Override
    public Object getMetaObject(Object tag, String key) {
        return null;
    }

    @Override
    public Object getTagValue(Object tag) throws IllegalAccessException, InvocationTargetException {
        return null;
    }

    @Override
    public void convertIntegers(Map<String, Object> m) {

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
    public void setTag(Object node, String tag, Object child) {

    }

    @Override
    public boolean setTag(ItemStack stack, String tag, Object child) {
        return false;
    }

    @Override
    public void setString(ItemStack stack, String tag, String value) {

    }

    @Override
    public void setString(Object node, String tag, String value) {

    }

    @Override
    public void setLong(Object node, String tag, long value) {

    }

    @Override
    public void setBoolean(Object node, String tag, boolean value) {

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
    public Boolean getOptionalBoolean(Object node, String tag) {
        return null;
    }

    @Override
    public byte[] getByteArray(Object tag, String key) {
        return new byte[0];
    }

    @Override
    public int[] getIntArray(Object tag, String key) {
        return new int[0];
    }
}
