package com.elmakers.mine.bukkit.utility.platform.v1_21_11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base_v1_21_4.ItemUtilsBase_v1_21_4;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

public class ItemUtils extends ItemUtilsBase_v1_21_4 {
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
        return customData == null ? null : (CompoundTag)ReflectionUtils.getPrivate(platform.getLogger(), customData, CustomData.class, "tag");
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
            tag = (CompoundTag)ReflectionUtils.getPrivate(platform.getLogger(), customData, CustomData.class, "tag");
            ((net.minecraft.world.item.ItemStack)mcItemStack).set(DataComponents.CUSTOM_DATA, customData);
        } else {
            tag = (CompoundTag)ReflectionUtils.getPrivate(platform.getLogger(), customData, CustomData.class, "tag");
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
        Optional<ListTag> listTagOptional = compoundTag.getList(key);

        if (listTagOptional.isPresent()) {
            ListTag listTag = listTagOptional.get();
            int size = listTag.size();
            for (int i = 0; i < size; i++) {
                Tag entry = listTag.get(i);
                Optional<String> optionalString = entry.asString();
                if (!optionalString.isPresent()) continue;
                list.add(optionalString.get());
            }
        }
        return list;
    }

    @Override
    public ItemStack getItem(Object itemTag) {
        // This was only called by getItems, which no longer uses this method
        throw new RuntimeException("The getItem method is no longer supported");

    }

    @Override
    public ItemStack[] getItems(Object rootTag, String tagName) {
        if (rootTag == null || !(rootTag instanceof CompoundTag)) return null;
        CompoundTag compoundTag = (CompoundTag)rootTag;

        try {
            CraftWorld world = (CraftWorld)Bukkit.getWorlds().get(0);
            ProblemReporter discard = ProblemReporter.DISCARDING;
            ValueInput valueInput = TagValueInput.create(discard, world.getHandle().registryAccess(), compoundTag);
            ValueInput.TypedInputList<ItemStackWithSlot> list = valueInput.listOrEmpty(tagName, ItemStackWithSlot.CODEC);
            List<ItemStack> itemList = new ArrayList<>();
            for (ItemStackWithSlot stackWithSlot : list) {
                ItemStack item = CraftItemStack.asCraftMirror(stackWithSlot.stack());
                itemList.add(item);
            }
            return itemList.toArray(new ItemStack[itemList.size()]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
