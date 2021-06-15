package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.ItemUtilsBase;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

public class ItemUtils extends ItemUtilsBase {
    public ItemUtils(Platform platform) {
        super(platform);
    }

    @Override
    public Object getHandle(org.bukkit.inventory.ItemStack stack) {
        if (stack == null) {
            return null;
        }
        // This may be a performance issue
        return ReflectionUtils.getPrivate(platform.getLogger(), stack, CraftItemStack.class, "handle");
    }

    @Override
    public CompoundTag getTag(Object mcItemStack) {
        if (mcItemStack == null || !(mcItemStack instanceof net.minecraft.world.item.ItemStack)) return null;
        net.minecraft.world.item.ItemStack itemStack = (net.minecraft.world.item.ItemStack)mcItemStack;
        return itemStack.getTag();
    }

    @Override
    public Object getTag(ItemStack itemStack) {
        Object tag = null;
        try {
            Object mcItemStack = getHandle(itemStack);
            if (mcItemStack == null) {
                if (itemStack.hasItemMeta()) {
                    itemStack = makeReal(itemStack);
                    mcItemStack = getHandle(itemStack);
                }
            }
            if (mcItemStack == null) return null;
            net.minecraft.world.item.ItemStack stack = (net.minecraft.world.item.ItemStack)mcItemStack;
            tag = stack.getTag();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return tag;
    }

    protected net.minecraft.world.item.ItemStack getNMSCopy(ItemStack stack) {
        net.minecraft.world.item.ItemStack nms = null;
        try {
            nms = CraftItemStack.asNMSCopy(stack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return nms;
    }

    @Override
    public ItemStack getCopy(ItemStack stack) {
        if (stack == null) return null;
        net.minecraft.world.item.ItemStack craft = getNMSCopy(stack);
        return CraftItemStack.asCraftMirror(craft);
    }

    @Override
    public ItemStack makeReal(ItemStack stack) {
        if (stack == null) return null;
        Object nmsStack = getHandle(stack);
        if (nmsStack == null) {
            stack = getCopy(stack);
            nmsStack = getHandle(stack);
        }
        if (nmsStack == null) {
            return null;
        }

        net.minecraft.world.item.ItemStack itemStack = (net.minecraft.world.item.ItemStack)nmsStack;
        CompoundTag tag = itemStack.getTag();
        if (tag == null) {
            itemStack.setTag(new CompoundTag());
        }

        return stack;
    }

    @Override
    public boolean isUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return false;
        ItemMeta meta = stack.getItemMeta();
        return meta.isUnbreakable();
    }

    @Override
    public void makeUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
    }

    @Override
    public void removeUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(false);
        stack.setItemMeta(meta);
    }

    @Override
    public void hideFlags(ItemStack stack, int flags) {
        if (isEmpty(stack)) return;

        try {
            Object craft = getHandle(stack);
            if (craft == null) return;
            CompoundTag tagObject = getTag(craft);
            if (tagObject == null) return;

            IntTag hideFlag = IntTag.valueOf(flags);
            tagObject.put("HideFlags", hideFlag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isEmpty(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return true;
        Object handle = getHandle(itemStack);
        if (handle == null || !(handle instanceof net.minecraft.world.item.ItemStack)) return false;
        net.minecraft.world.item.ItemStack mcItem = (net.minecraft.world.item.ItemStack)handle;
        return mcItem.isEmpty();
    }

    protected Object getTagString(String value) {
        return StringTag.valueOf(value);
    }

    @Override
    public Object setStringList(Object nbtBase, String tag, Collection<String> values) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return null;
        CompoundTag compoundTag = (CompoundTag)nbtBase ;
        ListTag listMeta = new ListTag();

        for (String value : values) {
            Object nbtString = getTagString(value);
            platform.getNBTUtils().addToList(listMeta, nbtString);
        }

        compoundTag.put(tag, listMeta);
        return listMeta;
    }

    @Override
    public ItemStack getItem(Object itemTag) {
        if (itemTag == null || !(itemTag instanceof CompoundTag)) return null;
        ItemStack item = null;
        try {
            net.minecraft.world.item.ItemStack nmsStack = net.minecraft.world.item.ItemStack.of((CompoundTag)itemTag);
            item = CraftItemStack.asCraftMirror(nmsStack);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return item;
    }

    @Override
    public ItemStack[] getItems(Object rootTag, String tagName) {
        if (rootTag == null || !(rootTag instanceof CompoundTag)) return null;
        CompoundTag compoundTag = (CompoundTag)rootTag;
        try {
            ListTag itemList = compoundTag.getList(tagName, CompatibilityConstants.NBT_TYPE_COMPOUND);
            int size = itemList.size();
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                try {
                    Object itemData = itemList.get(i);
                    if (itemData != null) {
                        items[i] = getItem(itemData);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return items;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
