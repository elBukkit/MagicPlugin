package com.elmakers.mine.bukkit.utility.platform.base_v1_21_4;

import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.components.EquippableComponent;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.ItemUtilsBase;

public abstract class ItemUtilsBase_v1_21_4 extends ItemUtilsBase {
    public ItemUtilsBase_v1_21_4(Platform platform) {
        super(platform);
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

    public Object getEquippable(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemMeta.hasEquippable()) return null;
        return itemMeta.getEquippable();
    }

    public void setEquippable(ItemStack itemStack, Object equippable) {
        if (equippable == null || !(equippable instanceof EquippableComponent)) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        itemMeta.setEquippable((EquippableComponent)equippable);
        itemStack.setItemMeta(itemMeta);
    }
}
