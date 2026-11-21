package com.elmakers.mine.bukkit.utility.platform.base_v1_21_4;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.components.EquippableComponent;

import com.elmakers.mine.bukkit.utility.platform.Platform;

public abstract class ItemUtilsBase extends com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.ItemUtilsBase {
    public ItemUtilsBase(Platform platform) {
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

    @Override
    public String getArmorModel(ItemStack itemStack) {
        EquippableComponent equippable = getEquippableComponent(itemStack);
        if (equippable == null) return null;
        NamespacedKey model = equippable.getModel();
        return model == null ? null : model.toString();
    }

    @Override
    public void setArmorModel(ItemStack itemStack, String model) {
        NamespacedKey key = NamespacedKey.fromString(model);
        if (key == null) return;
        modifyEquippable(itemStack, equippable -> equippable.setModel(key));
    }

    @Override
    public String getArmorSlot(ItemStack itemStack) {
        EquippableComponent equippable = getEquippableComponent(itemStack);
        if (equippable == null) return null;
        EquipmentSlot slot = equippable.getSlot();
        return slot == null ? null : slot.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public void setArmorSlot(ItemStack itemStack, String slot) {
        EquipmentSlot equipmentSlot;
        try {
            equipmentSlot = EquipmentSlot.valueOf(slot.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return;
        }
        modifyEquippable(itemStack, equippable -> equippable.setSlot(equipmentSlot));
    }

    @Override
    public String getCameraOverlay(ItemStack itemStack) {
        EquippableComponent equippable = getEquippableComponent(itemStack);
        if (equippable == null) return null;
        NamespacedKey overlay = equippable.getCameraOverlay();
        return overlay == null ? null : overlay.toString();
    }

    @Override
    public void setCameraOverlay(ItemStack itemStack, String overlay) {
        NamespacedKey key = NamespacedKey.fromString(overlay);
        if (key == null) return;
        modifyEquippable(itemStack, equippable -> equippable.setCameraOverlay(key));
    }

    @Override
    public String getTooltipStyle(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasTooltipStyle()) return null;
        return itemMeta.getTooltipStyle().toString();
    }

    @Override
    public void setTooltipStyle(ItemStack itemStack, String style) {
        NamespacedKey key = NamespacedKey.fromString(style);
        if (key == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        itemMeta.setTooltipStyle(key);
        itemStack.setItemMeta(itemMeta);
    }

    private EquippableComponent getEquippableComponent(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasEquippable()) return null;
        return itemMeta.getEquippable();
    }

    private void modifyEquippable(ItemStack itemStack, Consumer<EquippableComponent> modifier) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        boolean hadEquippable = itemMeta.hasEquippable();
        EquippableComponent equippable = itemMeta.getEquippable();
        if (equippable == null) return;
        if (!hadEquippable) {
            EquipmentSlot defaultSlot = getDefaultArmorSlot(itemStack.getType());
            if (defaultSlot != null) {
                equippable.setSlot(defaultSlot);
            }
        }
        modifier.accept(equippable);
        itemMeta.setEquippable(equippable);
        itemStack.setItemMeta(itemMeta);
    }

    private static EquipmentSlot getDefaultArmorSlot(Material material) {
        String name = material.name();
        if (name.endsWith("_HELMET")) return EquipmentSlot.HEAD;
        if (name.endsWith("_CHESTPLATE") || name.equals("ELYTRA")) return EquipmentSlot.CHEST;
        if (name.endsWith("_LEGGINGS")) return EquipmentSlot.LEGS;
        if (name.endsWith("_BOOTS")) return EquipmentSlot.FEET;
        return null;
    }
}
