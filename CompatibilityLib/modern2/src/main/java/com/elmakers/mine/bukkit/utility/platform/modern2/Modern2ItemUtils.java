package com.elmakers.mine.bukkit.utility.platform.modern2;

import java.util.List;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.ItemUtilsBase;

public abstract class Modern2ItemUtils extends ItemUtilsBase {
    public Modern2ItemUtils(Platform platform) {
        super(platform);
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
        ItemMeta meta = stack.getItemMeta();
        ItemFlag[] flagArray = ItemFlag.values();
        for (int ordinal = 0; ordinal < flagArray.length; ordinal++) {
            ItemFlag flag = flagArray[ordinal];
            if ((flags & 1) == 1) {
                meta.addItemFlags(flag);
            } else {
                meta.removeItemFlags(flag);
            }
            flags >>= 1;
        }
        stack.setItemMeta(meta);
    }

    @Override
    public int getCustomModelData(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return 0;
        if (!itemMeta.hasCustomModelData()) return 0;
        CustomModelDataComponent component = itemMeta.getCustomModelDataComponent();
        // Spigot will just throw an error if there is only string data in here, which is not great behavior
        List<Float> floats = component.getFloats();
        if (floats.isEmpty()) return 0;
        return itemMeta.getCustomModelData();
    }

    @Override
    public void setCustomModelData(ItemStack itemStack, int customModelData) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        itemMeta.setCustomModelData(customModelData);
        itemStack.setItemMeta(itemMeta);
    }
}
