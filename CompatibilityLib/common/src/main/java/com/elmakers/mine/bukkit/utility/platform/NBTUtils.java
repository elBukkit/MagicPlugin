package com.elmakers.mine.bukkit.utility.platform;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public interface NBTUtils {

    boolean containsTag(ItemStack stack, String tag);

    Object getTag(ItemStack stack, String tag);

    Object getTag(Object nbtBase, String tag);

    Object createTag(Object nbtBase, String tag);

    Object createTag(ItemStack stack, String tag);

    boolean contains(Object nbtBase, String tag);

    String getString(Object node, String tag, String defaultValue);

    String getString(Object node, String tag);

    String getString(ItemStack stack, String tag);

    String getString(ItemStack stack, String tag, String defaultValue);

    Byte getOptionalByte(Object node, String tag);

    Short getOptionalShort(Object node, String tag);

    short getShort(Object node, String tag, short defaultValue);

    Integer getOptionalInt(Object node, String tag);

    int getInt(Object node, String tag, int defaultValue);

    int getInt(ItemStack stack, String tag, int defaultValue);

    Double getOptionalDouble(Object node, String tag);

    boolean getBoolean(ItemStack stack, String tag, boolean defaultValue);

    Boolean getOptionalBoolean(Object node, String tag);

    byte[] getByteArray(Object tag, String key);

    int[] getIntArray(Object tag, String key);

    void parseAndSet(Object node, String tag, String value);

    void setTag(Object node, String tag, Object child);

    boolean setTag(ItemStack stack, String tag, Object child);

    void setString(ItemStack stack, String tag, String value);

    void setString(Object node, String tag, String value);

    void setLong(Object node, String tag, long value);

    void setBoolean(Object node, String tag, boolean value);

    void setBoolean(ItemStack stack, String tag, boolean value);

    void setDouble(Object node, String tag, double value);

    void setInt(Object node, String tag, int value);

    void setInt(ItemStack stack, String tag, int value);

    void setMetaShort(Object node, String tag, short value);

    void setIntArray(Object tag, String key, int[] value);

    void setByteArray(Object tag, String key, byte[] value);

    void setEmptyList(Object tag, String key);

    void removeMeta(Object node, String tag);

    void removeMeta(ItemStack stack, String tag);

    void addToList(Object listObject, Object node);

    Object readTagFromStream(InputStream input);

    boolean writeTagToStream(Object tag, OutputStream output);

    Set<String> getAllKeys(Object tag);

    Collection<Object> getTagList(Object tag, String key);

    Object newCompoundTag();

    boolean setSpawnEggEntityData(ItemStack spawnEgg, Entity entity, Object entityData);

    boolean saveTagsToItem(ConfigurationSection tags, ItemStack item);

    boolean saveTagsToNBT(ConfigurationSection tags, Object node);

    boolean saveTagsToNBT(ConfigurationSection tags, Object node, Set<String> tagNames);

    boolean saveTagsToNBT(Map<String, Object> tags, Object node, Set<String> tagNames);

    boolean addTagsToNBT(Map<String, Object> tags, Object node);

    Object wrapInTag(Object value)
            throws IllegalAccessException, InvocationTargetException, InstantiationException;

    Set<String> getTagKeys(Object tag);

    Object getMetaObject(Object tag, String key);

    Object getTagValue(Object tag) throws IllegalAccessException, InvocationTargetException;

    void convertIntegers(Map<String, Object> m);
}
