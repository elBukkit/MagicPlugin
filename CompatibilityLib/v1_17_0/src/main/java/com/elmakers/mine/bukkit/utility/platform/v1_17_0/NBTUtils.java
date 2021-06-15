package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.NBTUtilsBase;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
    public Byte getMetaByte(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        return ((CompoundTag)node).getByte(tag);
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
    public void setMeta(Object node, String tag, String value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putString(tag, value);
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
    public void removeMeta(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).remove(tag);
    }

    @Override
    public void setMetaNode(Object node, String tag, Object child) {
        if (node == null || !(node instanceof CompoundTag)) return;
        if (child == null) {
            ((CompoundTag)node).remove(tag);
        } else if (child instanceof Tag){
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
    public String getMetaString(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return null;
        String meta = null;
        Object tagObject = platform.getItemUtils().getTag(stack);
        if (tagObject == null || !(tagObject instanceof CompoundTag)) return null;
        meta = ((CompoundTag)tagObject).getString(tag);
        return meta;
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
    public void addToList(Object listObject, Object node) {
        if (listObject == null || !(listObject instanceof ListTag) || !(node instanceof Tag)) return;
        ListTag list = (ListTag)listObject;
        list.add((Tag)node);
    }
}
