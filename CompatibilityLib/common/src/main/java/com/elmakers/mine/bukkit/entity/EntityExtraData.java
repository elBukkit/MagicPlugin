package com.elmakers.mine.bukkit.entity;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;

public abstract class EntityExtraData {

    public abstract void apply(Entity entity);

    public void cycle(Entity entity) {
    }

    public static Platform getPlatform() {
        return PlatformInterpreter.getPlatform();
    }

    @Nullable
    protected ItemData getItem(ItemStack item, MageController controller) {
        return item == null ? null : controller.createItemData(item);
    }

    public boolean isSplittable() {
        return true;
    }

    public byte getMaterialData() {
        return 0;
    }

    public Material getMaterial() {
        return null;
    }

    public MaterialAndData getMaterialAndData() {
        return null;
    }

    public void setMaterialAndData(MaterialAndData material) {

    }
}
