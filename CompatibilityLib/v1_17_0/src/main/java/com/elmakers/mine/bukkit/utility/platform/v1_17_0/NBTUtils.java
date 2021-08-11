package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.NBTUtilsBase;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

public class NBTUtils extends NBTUtilsBase {
    public NBTUtils(Platform platform) {
        super(platform);
    }

    @Override
    public Object getNode(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return null;
        Object tagObject = platform.getItemUtils().getTag(stack);
        if (tagObject == null || !(tagObject instanceof CompoundTag)) return null;
        return ((CompoundTag)tagObject).get(tag);
    }

    @Override
    public Object getNode(Object nbtBase, String tag) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return null;
        return ((CompoundTag)nbtBase).get(tag);
    }

    @Override
    public Set<String> getAllKeys(Object nbtBase) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return null;
        return ((CompoundTag)nbtBase).getAllKeys();
    }

    @Override
    public boolean containsNode(Object nbtBase, String tag) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return false;
        return ((CompoundTag)nbtBase).contains(tag);
    }

    @Override
    public Object createNode(Object nbtBase, String tag) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return null;

        CompoundTag compoundTag = (CompoundTag)nbtBase;
        CompoundTag meta = compoundTag.getCompound(tag);
        // Strangely getCompound always returns non-null, but the tag it returns
        // if not found in the parent is not connected to the parent.
        compoundTag.put(tag, meta);
        return meta;
    }

    @Override
    public Object createNode(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return null;
        Object outputObject = getNode(stack, tag);
        if (outputObject == null || !(outputObject instanceof CompoundTag)) {
            Object craft = platform.getItemUtils().getHandle(stack);
            if (craft == null) return null;
            CompoundTag tagObject = (CompoundTag)platform.getItemUtils().getTag(craft);
            if (tagObject == null) {
                tagObject = new CompoundTag();
                ((net.minecraft.world.item.ItemStack)craft).setTag(tagObject);
            }
            outputObject = new CompoundTag();
            tagObject.put(tag, (CompoundTag)outputObject);
        }
        return outputObject;
    }

    @Override
    public String getMetaString(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        return ((CompoundTag)node).getString(tag);
    }

    @Override
    public String getMetaString(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return null;
        String meta = null;
        Object tagObject = platform.getItemUtils().getTag(stack);
        if (tagObject == null || !(tagObject instanceof CompoundTag)) return null;
        meta = ((CompoundTag)tagObject).getString(tag);
        return meta;
    }

    @Override
    public Byte getMetaByte(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        return ((CompoundTag)node).getByte(tag);
    }

    @Override
    public Short getMetaShort(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        return ((CompoundTag)node).getShort(tag);
    }

    @Override
    public Integer getMetaInt(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        return ((CompoundTag)node).getInt(tag);
    }

    @Override
    public Double getMetaDouble(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        return ((CompoundTag)node).getDouble(tag);
    }

    @Override
    public Boolean getMetaBoolean(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        return ((CompoundTag)node).getBoolean(tag);
    }

    @Override
    public byte[] getByteArray(Object tag, String key) {
        if (tag == null || !(tag instanceof CompoundTag)) return null;
        return ((CompoundTag)tag).getByteArray(key);
    }

    @Override
    public int[] getIntArray(Object tag, String key) {
        if (tag == null || !(tag instanceof CompoundTag)) return null;
        return ((CompoundTag)tag).getIntArray(key);
    }

    @Override
    public void setMetaLong(Object node, String tag, long value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putLong(tag, value);
    }

    @Override
    public void setMetaBoolean(Object node, String tag, boolean value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putBoolean(tag, value);
    }

    @Override
    public void setMetaDouble(Object node, String tag, double value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putDouble(tag, value);
    }

    @Override
    public void setMetaInt(Object node, String tag, int value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putInt(tag, value);
    }

    @Override
    public void setMetaShort(Object node, String tag, short value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putShort(tag, value);
    }

    @Override
    public void removeMeta(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).remove(tag);
    }

    @Override
    public void setMetaNode(Object node, String tag, Object child) {
        if (node == null || !(node instanceof CompoundTag)) return;
        if (child == null) {
            ((CompoundTag)node).remove(tag);
        } else if (child instanceof Tag) {
            ((CompoundTag)node).put(tag, (Tag)child);
        }
    }

    @Override
    public boolean setMetaNode(ItemStack stack, String tag, Object child) {
        if (platform.getItemUtils().isEmpty(stack)) return false;
        Object craft = platform.getItemUtils().getHandle(stack);
        if (craft == null) return false;
        Object node = platform.getItemUtils().getTag(craft);
        if (node == null || !(node instanceof CompoundTag)) return false;
        if (child == null) {
            ((CompoundTag)node).remove(tag);
        } else {
            ((CompoundTag)node).put(tag, (Tag)child);
        }
        return true;
    }

    @Override
    public void setMeta(Object node, String tag, String value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putString(tag, value);
    }

    @Override
    public void setMeta(ItemStack stack, String tag, String value) {
        if (platform.getItemUtils().isEmpty(stack)) return;
        Object craft = platform.getItemUtils().getHandle(stack);
        if (craft == null) return;
        Object tagObject = platform.getItemUtils().getTag(craft);
        if (tagObject == null || !(tagObject instanceof CompoundTag)) return;
        ((CompoundTag)tagObject).putString(tag, value);
    }

    @Override
    public void putIntArray(Object tag, String key, int[] value) {
        if (tag == null || !(tag instanceof CompoundTag)) return;
        ((CompoundTag)tag).put(key, new IntArrayTag(value));
    }

    @Override
    public void putByteArray(Object tag, String key, byte[] value) {
        if (tag == null || !(tag instanceof CompoundTag)) return;
        ((CompoundTag)tag).put(key, new ByteArrayTag(value));
    }

    @Override
    public void putEmptyList(Object tag, String key) {
        if (tag == null || !(tag instanceof CompoundTag)) return;
        ((CompoundTag)tag).put(key, new ListTag());
    }

    @Override
    public void addToList(Object listObject, Object node) {
        if (listObject == null || !(listObject instanceof ListTag) || !(node instanceof Tag)) return;
        ListTag list = (ListTag)listObject;
        list.add((Tag)node);
    }

    @Override
    public Object readTagFromStream(InputStream input) {
        CompoundTag tag = null;
        try {
            tag = NbtIo.readCompressed(input);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error reading from NBT input stream", ex);
        }
        return tag;
    }

    @Override
    public boolean writeTagToStream(Object tag, OutputStream output) {
        if (tag == null || !(tag instanceof CompoundTag)) return false;
        try {
            NbtIo.writeCompressed((CompoundTag)tag, output);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error writing NBT output stream", ex);
            return false;
        }
        return true;
    }

    @Override
    public Collection<Object> getTagList(Object tag, String key) {
        Collection<Object> list = new ArrayList<>();
        if (tag == null || !(tag instanceof ListTag)) {
            return list;
        }

        ListTag listTag = ((CompoundTag)tag).getList(key, CompatibilityConstants.NBT_TYPE_COMPOUND);
        if (listTag != null) {
            int size = listTag.size();
            for (int i = 0; i < size; i++) {
                Tag entry = listTag.get(i);
                list.add(entry);
            }
        }
        return list;
    }

    @Override
    public Object newCompoundTag() {
        return new CompoundTag();
    }
}
