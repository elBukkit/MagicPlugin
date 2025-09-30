package com.elmakers.mine.bukkit.utility.platform.base;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.ConfigUtils;
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
    public boolean setSpawnEggEntityData(ItemStack spawnEgg, Entity entity, Object entityData) {
        return setTag(spawnEgg, "EntityTag", entityData);
    }

    @Override
    public boolean saveTagsToItem(ConfigurationSection tags, ItemStack item) {
        Object handle = platform.getItemUtils().getHandle(item);
        if (handle == null) return false;
        Object tag = platform.getItemUtils().getOrCreateTag(handle);
        if (tag == null) return false;

        return addTagsToNBT(ConfigUtils.toMap(tags), tag);
    }

    @Override
    public boolean saveTagsToNBT(ConfigurationSection tags, Object node) {
        return saveTagsToNBT(tags, node, null);
    }

    @Override
    public boolean saveTagsToNBT(ConfigurationSection tags, Object node, Set<String> tagNames) {
        return saveTagsToNBT(ConfigUtils.toMap(tags), node, tagNames);
    }

    @Override
    public void convertIntegers(Map<String, Object> m) {
        for (Map.Entry<String, Object> entry : m.entrySet()) {
            Object value = entry.getValue();
            if (value != null && value instanceof Double) {
                double d = (Double) value;
                if (d == (int)d) {
                    entry.setValue((int)d);
                }
            } else if (value != null && value instanceof Float) {
                float f = (Float) value;
                if (f == (int)f) {
                    entry.setValue((int)f);
                }
            } else if (value != null && value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>)value;
                convertIntegers(map);
            }
        }
    }

    protected byte[] makeByteArray(List<Object> list) {
        byte[] a = new byte[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            Byte b = (Byte)list.get(i);
            a[i] = b == null ? 0 : b;
        }

        return a;
    }

    protected int[] makeIntArray(List<Object> list) {
        int[] a = new int[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            Integer value = (Integer)list.get(i);
            a[i] = value == null ? 0 : value;
        }

        return a;
    }

    protected long[]  makeLongArray(List<Object> list) {
        long[] a = new long[list.size()];

        for (int i = 0; i < list.size(); ++i) {
            Long l = (Long)list.get(i);
            a[i] = l == null ? 0 : l;
        }

        return a;
    }

    protected Long convertToLong(Object o) {
        if (o == null) return null;
        if (o instanceof Long) return (Long)o;
        if (o instanceof Integer) return (long)(Integer)o;
        if (o instanceof Byte) return (long)(Byte)o;
        if (o instanceof Double) return (long)(double)(Double)o;
        if (o instanceof String) return Long.parseLong((String)o);
        return null;
    }

    protected Integer convertToInteger(Object o) {
        Long intVal = convertToLong(o);
        return intVal == null ? null : (int)(long)intVal;
    }

    protected Byte convertToByte(Object o) {
        Long intVal = convertToLong(o);
        return intVal == null ? null : (byte)(long)intVal;
    }

    protected Short convertToShort(Object o) {
        Long intVal = convertToLong(o);
        return intVal == null ? null : (short)(long)intVal;
    }

    protected Double convertToDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Double) return (Double)o;
        if (o instanceof Integer) return (double)(Integer)o;
        if (o instanceof Long) return (double)(Long)o;
        if (o instanceof Byte) return (double)(Byte)o;
        if (o instanceof String) return Double.parseDouble((String)o);
        return null;
    }
}
