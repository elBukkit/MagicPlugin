package com.elmakers.mine.bukkit.utility.platform.v1_20_4;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.NBTUtilsBase;

public class NBTUtilsPlaceholder extends NBTUtilsBase {
    public NBTUtilsPlaceholder(Platform platform) {
        super(platform);
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
    public Set<String> getAllKeys(Object nbtBase) {
        return null;
    }

    @Override
    public boolean contains(Object nbtBase, String tag) {
        return false;
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
    public byte[] getByteArray(Object tag, String key) {
        return null;
    }

    @Override
    public int[] getIntArray(Object tag, String key) {
        return null;
    }

    @Override
    public String getString(Object node, String tag) {
        return null;
    }

    @Override
    public String getString(ItemStack stack, String tag) {
        return null;
    }

    @Override
    public Byte getOptionalByte(Object node, String tag) {
        return null;
    }

    @Override
    public Integer getOptionalInt(Object node, String tag) {
        return null;
    }

    @Override
    public Short getOptionalShort(Object node, String tag) {
        return null;
    }

    @Override
    public Double getOptionalDouble(Object node, String tag) {
        return null;
    }

    @Override
    public Boolean getOptionalBoolean(Object node, String tag) {
        return null;
    }

    @Override
    public void setLong(Object node, String tag, long value) {
    }

    @Override
    public void setBoolean(Object node, String tag, boolean value) {
    }

    @Override
    public void setDouble(Object node, String tag, double value) {
    }

    @Override
    public void setInt(Object node, String tag, int value) {
    }

    @Override
    public void setMetaShort(Object node, String tag, short value) {
    }

    @Override
    public void removeMeta(Object node, String tag) {
    }

    @Override
    public void setTag(Object node, String tag, Object child) {
    }

    @Override
    public boolean setTag(ItemStack stack, String tag, Object child) {
        return false;
    }

    @Override
    public void setString(Object node, String tag, String value) {
    }

    @Override
    public void setString(ItemStack stack, String tag, String value) {
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
    public Collection<Object> getTagList(Object tag, String key) {
        return null;
    }

    @Override
    public Object newCompoundTag() {
        return null;
    }
}
