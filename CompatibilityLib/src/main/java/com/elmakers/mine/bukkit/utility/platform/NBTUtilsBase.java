package com.elmakers.mine.bukkit.utility.platform;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class NBTUtilsBase implements NBTUtils {

    @Override
    public String getMetaString(ItemStack stack, String tag, String defaultValue) {
        String result = NBTUtilsBase.this.getMetaString(stack, tag);
        return result == null ? defaultValue : result;
    }

    @Override
    public boolean hasMeta(ItemStack stack, String tag) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return false;
        return NBTUtilsBase.this.getNode(stack, tag) != null;
    }

    @Override
    public Object getNode(ItemStack stack, String tag) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return null;
        Object meta = null;
        try {
            Object tagObject = CompatibilityLib.getItemUtils().getTag(stack);
            if (tagObject == null) return null;
            meta = NMSUtils.class_NBTTagCompound_getMethod.invoke(tagObject, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public boolean containsNode(Object nbtBase, String tag) {
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
    public Object getNode(Object nbtBase, String tag) {
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
    public Object createNode(Object nbtBase, String tag) {
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
    public Object createNode(ItemStack stack, String tag) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return null;
        Object outputObject = NBTUtilsBase.this.getNode(stack, tag);
        if (outputObject == null) {
            try {
                Object craft = CompatibilityLib.getItemUtils().getHandle(stack);
                if (craft == null) return null;
                Object tagObject = CompatibilityLib.getItemUtils().getTag(craft);
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
    public String getMetaString(Object node, String tag, String defaultValue) {
        String meta = NBTUtilsBase.this.getMetaString(node, tag);
        return meta == null || meta.length() == 0 ? defaultValue : meta;
    }

    @Override
    public String getMetaString(Object node, String tag) {
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
    public String getMeta(Object node, String tag) {
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
    public Byte getMetaByte(Object node, String tag) {
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
    public Integer getMetaInt(Object node, String tag) {
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
    public int getMetaInt(ItemStack stack, String tag, int defaultValue) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return defaultValue;
        int result = defaultValue;
        try {
            Object tagObject = CompatibilityLib.getItemUtils().getTag(stack);
            if (tagObject == null) return defaultValue;
            Integer value = NBTUtilsBase.this.getMetaInt(tagObject, tag);
            result = value == null ? defaultValue : value;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public Double getMetaDouble(Object node, String tag) {
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
    public Boolean getMetaBoolean(Object node, String tag) {
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
    public void setMeta(Object node, String tag, String value) {
        if (node == null|| !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
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
    public void setMetaLong(Object node, String tag, long value) {
        if (node == null|| !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_setLongMethod.invoke(node, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setMetaBoolean(Object node, String tag, boolean value) {
        if (node == null|| !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_setBooleanMethod.invoke(node, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setMetaDouble(Object node, String tag, double value) {
        if (node == null|| !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_setDoubleMethod.invoke(node, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setMetaInt(Object node, String tag, int value) {
        if (node == null|| !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_setIntMethod.invoke(node, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setMetaInt(ItemStack stack, String tag, int value) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return;
        try {
            Object craft = CompatibilityLib.getItemUtils().getHandle(stack);
            if (craft == null) return;
            Object tagObject = CompatibilityLib.getItemUtils().getTag(craft);
            if (tagObject == null) return;
            NBTUtilsBase.this.setMetaInt(tagObject, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeMeta(Object node, String tag) {
        if (node == null|| !NMSUtils.class_NBTTagCompound.isInstance(node)) return;
        try {
            NMSUtils.class_NBTTagCompound_removeMethod.invoke(node, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeMeta(ItemStack stack, String tag) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return;

        try {
            Object craft = CompatibilityLib.getItemUtils().getHandle(stack);
            if (craft == null) return;
            Object tagObject = CompatibilityLib.getItemUtils().getTag(craft);
            if (tagObject == null) return;
            NBTUtilsBase.this.removeMeta(tagObject, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setMetaTyped(Object node, String tag, String value) {
        if (value == null) {
            NBTUtilsBase.this.removeMeta(node, tag);
            return;
        }

        boolean isTrue = value.equals("true");
        boolean isFalse = value.equals("false");
        if (isTrue || isFalse) {
            NBTUtilsBase.this.setMetaBoolean(node, tag, isTrue);
        } else {
            try {
                Integer i = Integer.parseInt(value);
                NBTUtilsBase.this.setMetaInt(node, tag, i);
            } catch (Exception ex) {
                try {
                    Double d = Double.parseDouble(value);
                    NBTUtilsBase.this.setMetaDouble(node, tag, d);
                } catch (Exception ex2) {
                    NBTUtilsBase.this.setMeta(node, tag, value);
                }
            }
        }
    }

    @Override
    public void setMetaNode(Object node, String tag, Object child) {
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
    public boolean setMetaNode(ItemStack stack, String tag, Object child) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return false;
        try {
            Object craft = CompatibilityLib.getItemUtils().getHandle(stack);
            if (craft == null) return false;
            Object node = CompatibilityLib.getItemUtils().getTag(craft);
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
    public String getMetaString(ItemStack stack, String tag) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return null;
        String meta = null;
        try {
            Object tagObject = CompatibilityLib.getItemUtils().getTag(stack);
            if (tagObject == null) return null;
            meta = (String) NMSUtils.class_NBTTagCompound_getStringMethod.invoke(tagObject, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    @Override
    public void setMeta(ItemStack stack, String tag, String value) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return;
        try {
            Object craft = CompatibilityLib.getItemUtils().getHandle(stack);
            if (craft == null) return;
            Object tagObject = CompatibilityLib.getItemUtils().getTag(craft);
            if (tagObject == null) return;
            NMSUtils.class_NBTTagCompound_setStringMethod.invoke(tagObject, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setMetaBoolean(ItemStack stack, String tag, boolean value) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return;
        try {
            Object craft = CompatibilityLib.getItemUtils().getHandle(stack);
            if (craft == null) return;
            Object tagObject = CompatibilityLib.getItemUtils().getTag(craft);
            if (tagObject == null) return;
            NBTUtilsBase.this.setMetaBoolean(tagObject, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean getMetaBoolean(ItemStack stack, String tag, boolean defaultValue) {
        if (CompatibilityLib.getItemUtils().isEmpty(stack)) return defaultValue;
        boolean result = defaultValue;
        try {
            Object tagObject = CompatibilityLib.getItemUtils().getTag(stack);
            if (tagObject == null) return defaultValue;
            Boolean value = NBTUtilsBase.this.getMetaBoolean(tagObject, tag);
            result = value == null ? defaultValue : value;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return result;
    }

    protected void addToList(Object listObject, Object node) throws InvocationTargetException, IllegalAccessException {
        if (NMSUtils.isCurrentVersion) {
            int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(listObject);
            NMSUtils.class_NBTTagList_addMethod.invoke(listObject, size, node);
        } else {
            NMSUtils.class_NBTTagList_addMethod.invoke(listObject, node);
        }
    }
}
