package com.elmakers.mine.bukkit.utility.platform.base;

import org.bukkit.inventory.ItemStack;

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
            Object tagObject = platform.getItemUtils().getTag(craft);
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
            Object tagObject = platform.getItemUtils().getTag(craft);
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
}
