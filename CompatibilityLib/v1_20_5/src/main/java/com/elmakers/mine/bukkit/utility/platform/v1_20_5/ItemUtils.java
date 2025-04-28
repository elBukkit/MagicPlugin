package com.elmakers.mine.bukkit.utility.platform.v1_20_5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.ItemUtilsBase;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;

public class ItemUtils extends ItemUtilsBase {
    public ItemUtils(Platform platform) {
        super(platform);
    }

    @Override
    public Object getHandle(org.bukkit.inventory.ItemStack stack) {
        if (stack == null || !(stack instanceof CraftItemStack)) {
            return null;
        }
        return ReflectionUtils.getHandle(platform.getLogger(), stack, CraftItemStack.class);
    }

    @Override
    public CompoundTag getTag(Object mcItemStack) {
        if (mcItemStack == null || !(mcItemStack instanceof net.minecraft.world.item.ItemStack)) return null;
        net.minecraft.world.item.ItemStack itemStack = (net.minecraft.world.item.ItemStack)mcItemStack;
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        return customData == null ? null : customData.getUnsafe();
    }

    @Override
    public CompoundTag getTag(ItemStack itemStack) {
        CompoundTag tag = null;
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
            tag = getTag(stack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return tag;
    }

    @Override
    public CompoundTag getOrCreateTag(Object mcItemStack) {
        if (mcItemStack == null || !(mcItemStack instanceof net.minecraft.world.item.ItemStack)) return null;
        net.minecraft.world.item.ItemStack itemStack = (net.minecraft.world.item.ItemStack)mcItemStack;
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = null;
        if (customData == null) {
            tag = new CompoundTag();
            // This makes a copy
            customData = CustomData.of(tag);
            tag = customData.getUnsafe();
            ((net.minecraft.world.item.ItemStack)mcItemStack).set(DataComponents.CUSTOM_DATA, customData);
        } else {
            tag = customData.getUnsafe();
        }
        return tag;
    }

    @Override
    public CompoundTag getOrCreateTag(ItemStack itemStack) {
        CompoundTag tag = null;
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
            tag = getOrCreateTag(stack);
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

        // Component patch can't be null I guess?
        // Not sure what happens if it's empty, though.
        /*
        net.minecraft.world.item.ItemStack itemStack = (net.minecraft.world.item.ItemStack)nmsStack;
        DataComponentPatch tag = itemStack.getComponentsPatch();
        if (tag == null) {
            itemStack.setTag(new CompoundTag());
        }
        */

        return stack;
    }

    @Override
    public boolean isEmpty(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return true;
        Object handle = getHandle(itemStack);
        if (handle == null || !(handle instanceof net.minecraft.world.item.ItemStack)) return false;
        net.minecraft.world.item.ItemStack mcItem = (net.minecraft.world.item.ItemStack)handle;
        return mcItem.isEmpty();
    }

    protected StringTag getTagString(String value) {
        return StringTag.valueOf(value);
    }

    @Override
    public Object setStringList(Object nbtBase, String tag, Collection<String> values) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return null;
        CompoundTag compoundTag = (CompoundTag)nbtBase;
        ListTag listTag = new ListTag();

        for (String value : values) {
            StringTag nbtString = getTagString(value);
            listTag.add(nbtString);
        }

        compoundTag.put(tag, listTag);
        return listTag;
    }

    @Override
    public List<String> getStringList(Object nbtBase, String key) {
        List<String> list = new ArrayList<>();
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return list;
        CompoundTag compoundTag = (CompoundTag)nbtBase;
        ListTag listTag = compoundTag.getList(key, CompatibilityConstants.NBT_TYPE_STRING);

        if (listTag != null) {
            int size = listTag.size();
            for (int i = 0; i < size; i++) {
                Tag entry = listTag.get(i);
                list.add(entry.getAsString());
            }
        }
        return list;
    }

    @Override
    public ItemStack getItem(Object itemTag) {
        if (itemTag == null || !(itemTag instanceof CompoundTag)) return null;
        ItemStack item = null;
        try {
            CraftWorld world = (CraftWorld)Bukkit.getWorlds().get(0);
            net.minecraft.world.item.ItemStack nmsStack = net.minecraft.world.item.ItemStack.parse(world.getHandle().registryAccess(), (CompoundTag)itemTag).get();
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
