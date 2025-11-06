package com.elmakers.mine.bukkit.entity;

import java.util.Iterator;
import javax.annotation.Nullable;

import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;

public abstract class EntityExtraData {

    public abstract void apply(Entity entity);

    public void applyPostSpawn(Entity entity) { }

    public boolean cycle(Entity entity) {
        return false;
    }

    public boolean canCycle(Entity entity) {
        return false;
    }

    public static Platform getPlatform() {
        return PlatformInterpreter.getPlatform();
    }

    @Nullable
    protected ItemData getItem(ItemStack item, MageController controller) {
        return item == null ? null : controller.createItemData(item);
    }

    // Here for slime-like mobs
    public boolean isSplittable() {
        return true;
    }

    // These are here for falling blocks
    public MaterialAndData getMaterialAndData() {
        return null;
    }

    public void setMaterialAndData(MaterialAndData material) {
    }

    // This is only used for specific entity types that require special spawning
    public SpawnedEntityExtraData spawn(Location location) {
        return null;
    }

    protected <T extends Keyed> T cycleRegistryValue(T value, Registry<T> registry) {
        Iterator<T> it = registry.iterator();
        while (it.hasNext()) {
            T testValue = it.next();
            if (value == testValue) {
                if (it.hasNext()) {
                    return it.next();
                } else {
                    return registry.iterator().next();
                }
            }
        }
        return value;
    }
}
