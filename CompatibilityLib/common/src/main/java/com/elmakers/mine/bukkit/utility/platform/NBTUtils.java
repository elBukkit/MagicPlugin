package com.elmakers.mine.bukkit.utility.platform;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

public interface NBTUtils {

    boolean hasMeta(ItemStack stack, String tag);

    Object getNode(ItemStack stack, String tag);

    Object getNode(Object nbtBase, String tag);

    Object createNode(Object nbtBase, String tag);

    Object createNode(ItemStack stack, String tag);

    boolean containsNode(Object nbtBase, String tag);

    String getMetaString(Object node, String tag, String defaultValue);

    String getMetaString(Object node, String tag);

    String getMetaString(ItemStack stack, String tag);

    String getMetaString(ItemStack stack, String tag, String defaultValue);

    String getMeta(Object node, String tag);

    Byte getMetaByte(Object node, String tag);

    Short getMetaShort(Object node, String tag);

    short getMetaShort(Object node, String tag, short defaultValue);

    Integer getMetaInt(Object node, String tag);

    int getMetaInt(Object node, String tag, int defaultValue);

    int getMetaInt(ItemStack stack, String tag, int defaultValue);

    Double getMetaDouble(Object node, String tag);

    boolean getMetaBoolean(ItemStack stack, String tag, boolean defaultValue);

    Boolean getMetaBoolean(Object node, String tag);

    byte[] getByteArray(Object tag, String key);

    int[] getIntArray(Object tag, String key);

    void setMetaTyped(Object node, String tag, String value);

    void setMetaNode(Object node, String tag, Object child);

    boolean setMetaNode(ItemStack stack, String tag, Object child);

    void setMeta(ItemStack stack, String tag, String value);

    void setMeta(Object node, String tag, String value);

    void setMetaLong(Object node, String tag, long value);

    void setMetaBoolean(Object node, String tag, boolean value);

    void setMetaBoolean(ItemStack stack, String tag, boolean value);

    void setMetaDouble(Object node, String tag, double value);

    void setMetaInt(Object node, String tag, int value);

    void setMetaInt(ItemStack stack, String tag, int value);

    void setMetaShort(Object node, String tag, short value);

    void putIntArray(Object tag, String key, int[] value);

    void putByteArray(Object tag, String key, byte[] value);

    void putEmptyList(Object tag, String key);

    void removeMeta(Object node, String tag);

    void removeMeta(ItemStack stack, String tag);

    void addToList(Object listObject, Object node);

    Object readTagFromStream(InputStream input);

    boolean writeTagToStream(Object tag, OutputStream output);

    Set<String> getAllKeys(Object tag);

    Collection<Object> getTagList(Object tag, String key);

    Object newCompoundTag();
}
