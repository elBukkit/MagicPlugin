package com.elmakers.mine.bukkit.utility.platform.base;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
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
    public void setInt(Object node, String tag, int value) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_setIntMethod.invoke(node, tag, value);
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
    public void removeMeta(Object node, String tag) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_removeMethod.invoke(node, tag);
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
    public void setBoolean(Object node, String tag, boolean value) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_setBooleanMethod.invoke(node, tag, value);
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
    public boolean setSpawnEggEntityData(ItemStack spawnEgg, Object entityData) {
        return setTag(spawnEgg, "EntityTag", entityData);
    }

    @Override
    public Object getTag(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return null;
        Object meta = null;
        try {
            Object tagObject = platform.getItemUtils().getTag(stack);
            if (tagObject == null) return null;
            meta = NMSUtils.class_NBTTagCompound_getMethod.invoke(tagObject, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public Object getTag(Object nbtBase, String tag) {
        if (nbtBase == null) return null;
        Object meta = null;
        try {
            meta = NMSUtils.class_NBTTagCompound_getMethod.invoke(nbtBase, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public boolean contains(Object nbtBase, String tag) {
        if (nbtBase == null) return false;
        Boolean result = false;
        try {
            result = (Boolean) NMSUtils.class_NBTTagCompound_hasKeyMethod.invoke(nbtBase, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public Object createTag(Object nbtBase, String tag) {
        if (nbtBase == null) return null;
        Object meta = null;
        try {
            meta = NMSUtils.class_NBTTagCompound_getCompoundMethod.invoke(nbtBase, tag);
            NMSUtils.class_NBTTagCompound_setMethod.invoke(nbtBase, tag, meta);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public Object createTag(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return null;
        Object outputObject = getTag(stack, tag);
        if (outputObject == null) {
            try {
                Object craft = platform.getItemUtils().getHandle(stack);
                if (craft == null) return null;
                Object tagObject = platform.getItemUtils().getTag(craft);
                if (tagObject == null) {
                    tagObject = NMSUtils.class_NBTTagCompound_constructor.newInstance();
                    NMSUtils.class_ItemStack_tagField.set(craft, tagObject);
                }
                outputObject = NMSUtils.class_NBTTagCompound_constructor.newInstance();
                NMSUtils.class_NBTTagCompound_setMethod.invoke(tagObject, tag, outputObject);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        return outputObject;
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
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return null;
        String meta = null;
        try {
            meta = (String) NMSUtils.class_NBTTagCompound_getStringMethod.invoke(node, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public String getString(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return null;
        String meta = null;
        try {
            Object tagObject = platform.getItemUtils().getTag(stack);
            if (tagObject == null) return null;
            meta = (String) NMSUtils.class_NBTTagCompound_getStringMethod.invoke(tagObject, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public Byte getOptionalByte(Object node, String tag) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return null;
        Byte meta = null;
        try {
            meta = (Byte) NMSUtils.class_NBTTagCompound_getByteMethod.invoke(node, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public Integer getOptionalInt(Object node, String tag) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return null;
        Integer meta = null;
        try {
            meta = (Integer) NMSUtils.class_NBTTagCompound_getIntMethod.invoke(node, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public Short getOptionalShort(Object node, String tag) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return null;
        Short meta = null;
        try {
            meta = (Short) NMSUtils.class_NBTTagCompound_getShortMethod.invoke(node, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public Double getOptionalDouble(Object node, String tag) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return null;
        Double meta = null;
        try {
            meta = (Double) NMSUtils.class_NBTTagCompound_getDoubleMethod.invoke(node, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public Boolean getOptionalBoolean(Object node, String tag) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return null;
        Boolean meta = null;
        try {
            meta = (Boolean) NMSUtils.class_NBTTagCompound_getBooleanMethod.invoke(node, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public void setString(Object node, String tag, String value) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            if (value == null || value.length() == 0) {
                NMSUtils.class_NBTTagCompound_removeMethod.invoke(node, tag);
            } else {
                NMSUtils.class_NBTTagCompound_setStringMethod.invoke(node, tag, value);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setString(ItemStack stack, String tag, String value) {
        if (platform.getItemUtils().isEmpty(stack)) return;
        try {
            Object craft = platform.getItemUtils().getHandle(stack);
            if (craft == null) return;
            Object tagObject = platform.getItemUtils().getOrCreateTag(craft);
            if (tagObject == null) return;
            NMSUtils.class_NBTTagCompound_setStringMethod.invoke(tagObject, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setLong(Object node, String tag, long value) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_setLongMethod.invoke(node, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setDouble(Object node, String tag, double value) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_setDoubleMethod.invoke(node, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setMetaShort(Object node, String tag, short value) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_setShortMethod.invoke(node, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
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
    public void setTag(Object node, String tag, Object child) {
        if (node == null || !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            if (child == null) {
                NMSUtils.class_NBTTagCompound_removeMethod.invoke(node, tag);
            } else {
                NMSUtils.class_NBTTagCompound_setMethod.invoke(node, tag, child);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean setTag(ItemStack stack, String tag, Object child) {
        if (platform.getItemUtils().isEmpty(stack)) return false;
        try {
            Object craft = platform.getItemUtils().getHandle(stack);
            if (craft == null) return false;
            Object node = platform.getItemUtils().getOrCreateTag(craft);
            if (node == null) return false;
            if (child == null) {
                NMSUtils.class_NBTTagCompound_removeMethod.invoke(node, tag);
            } else {
                NMSUtils.class_NBTTagCompound_setMethod.invoke(node, tag, child);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void addToList(Object listObject, Object node) {
        try {
            if (NMSUtils.isCurrentVersion) {
                int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(listObject);
                NMSUtils.class_NBTTagList_addMethod.invoke(listObject, size, node);
            } else {
                NMSUtils.class_NBTTagList_addMethod.invoke(listObject, node);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Object readTagFromStream(InputStream input) {
        Object tag = null;
        try {
            tag = NMSUtils.class_NBTCompressedStreamTools_loadFileMethod.invoke(null, input);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error reading from NBT input stream", ex);
        }
        return tag;
    }

    @Override
    public boolean writeTagToStream(Object tag, OutputStream output) {
        // Not in legacy versions
        return false;
    }

    @Override
    public byte[] getByteArray(Object tag, String key) {
        byte[] a = null;
        try {
            a = (byte[]) NMSUtils.class_NBTTagCompound_getByteArrayMethod.invoke(tag, key);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error reading byte array from tag", ex);
        }
        return a;
    }

    @Override
    public int[] getIntArray(Object tag, String key) {
        int[] a = null;
        try {
            a = (int[]) NMSUtils.class_NBTTagCompound_getIntArrayMethod.invoke(tag, key);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error reading int array from tag", ex);
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getAllKeys(Object nbtBase) {
        Set<String> keys = null;
        try {
            keys = (Set<String>) NMSUtils.class_NBTTagCompound_getKeysMethod.invoke(nbtBase);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error reading keys from tag", ex);
        }
        return keys;
    }

    @Override
    public Collection<Object> getTagList(Object tag, String key) {
        Collection<Object> list = new ArrayList<>();

        try {
            Object listTag = NMSUtils.class_NBTTagCompound_getListMethod.invoke(tag, key, CompatibilityConstants.NBT_TYPE_COMPOUND);
            if (listTag != null) {
                int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(listTag);
                for (int i = 0; i < size; i++) {
                    Object entity = NMSUtils.class_NBTTagList_getMethod.invoke(listTag, i);
                    list.add(entity);
                }
            }
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error reading list from tag", ex);
        }
        return list;
    }

    @Override
    public Object newCompoundTag() {
        Object tag = null;
        try {
            tag = NMSUtils.class_NBTTagCompound_constructor.newInstance();
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error creating new Compoundtag", ex);
        }
        return tag;
    }
}
