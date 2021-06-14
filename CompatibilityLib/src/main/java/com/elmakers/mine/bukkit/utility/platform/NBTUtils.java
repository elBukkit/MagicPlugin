package com.elmakers.mine.bukkit.utility.platform;

import org.bukkit.inventory.ItemStack;

public interface NBTUtils {
    String getMetaString(ItemStack stack, String tag, String defaultValue);

    boolean hasMeta(ItemStack stack, String tag);

    Object getNode(ItemStack stack, String tag);

    boolean containsNode(Object nbtBase, String tag);

    Object getNode(Object nbtBase, String tag);

    Object createNode(Object nbtBase, String tag);

    Object createNode(ItemStack stack, String tag);

    String getMetaString(Object node, String tag, String defaultValue);

    String getMetaString(Object node, String tag);

    String getMeta(Object node, String tag);

    Byte getMetaByte(Object node, String tag);

    Integer getMetaInt(Object node, String tag);

    int getMetaInt(ItemStack stack, String tag, int defaultValue);

    Double getMetaDouble(Object node, String tag);

    Boolean getMetaBoolean(Object node, String tag);

    void setMeta(Object node, String tag, String value);

    void setMetaLong(Object node, String tag, long value);

    void setMetaBoolean(Object node, String tag, boolean value);

    void setMetaDouble(Object node, String tag, double value);

    void setMetaInt(Object node, String tag, int value);

    void setMetaInt(ItemStack stack, String tag, int value);

    void removeMeta(Object node, String tag);

    void removeMeta(ItemStack stack, String tag);

    void setMetaTyped(Object node, String tag, String value);

    void setMetaNode(Object node, String tag, Object child);

    boolean setMetaNode(ItemStack stack, String tag, Object child);

    String getMetaString(ItemStack stack, String tag);

    void setMeta(ItemStack stack, String tag, String value);

    void setMetaBoolean(ItemStack stack, String tag, boolean value);

    boolean getMetaBoolean(ItemStack stack, String tag, boolean defaultValue);
}
