package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityEnderSignalData extends EntityAnimalData {
    private Boolean dropItem;
    private Integer despawnTimer;
    private Location targetLocation;
    private Object configLocation;
    private ItemStack item;

    public EntityEnderSignalData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        dropItem = ConfigUtils.getOptionalBoolean(parameters, "drop_item");
        if (parameters.contains("despawn_timer"))
            despawnTimer = parameters.getInt("despawn_timer");
        else if (parameters.contains("duration")) {
            // these despawn when they hit 80 ticks
            int duration = parameters.getInt("duration");
            despawnTimer = 80 - (duration / 50);
        }
        String itemKey = parameters.getString("item");
        if (itemKey != null && !itemKey.isEmpty()) {
            ItemData itemData = controller.getOrCreateItem(itemKey);
            if (itemData == null) {
                controller.getLogger().warning("Invalid item in ender_signal.item config: " + itemKey);
            } else {
                item = itemData.getItemStack();
            }
        }
        configLocation = parameters.get("target_location");
    }

    public EntityEnderSignalData(Entity entity) {
        super(entity);
        if (entity instanceof EnderSignal) {
            EnderSignal signal = (EnderSignal)entity;
            dropItem = signal.getDropItem();
            despawnTimer = signal.getDespawnTimer();
            targetLocation = signal.getTargetLocation();
            item = signal.getItem();
        }
    }

    @Override
    public void applyPostSpawn(Entity entity) {
        super.applyPostSpawn(entity);
        if (entity instanceof EnderSignal) {
            // These get reset when adding the signal to the world, so we will apply them again
            EnderSignal signal = (EnderSignal)entity;
            if (dropItem != null) {
                signal.setDropItem(dropItem);
            }
            if (despawnTimer != null) {
                signal.setDespawnTimer(despawnTimer);
            }
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof EnderSignal) {
            EnderSignal signal = (EnderSignal)entity;
            if (dropItem != null) {
                signal.setDropItem(dropItem);
            }
            if (despawnTimer != null) {
                signal.setDespawnTimer(despawnTimer);
            }
            if (targetLocation != null) {
                signal.setTargetLocation(targetLocation);
            } else if (configLocation != null) {
                Location relativeTarget = ConfigUtils.toLocation(configLocation, entity.getLocation());
                signal.setTargetLocation(relativeTarget);
            }
            if (item != null) {
                signal.setItem(item);
            }
        }
    }
}
