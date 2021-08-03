package com.elmakers.mine.bukkit.utility.platform.v1_17_1.entity;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAnimalData;

public class EntityEnderSignalData extends EntityAnimalData {
    private Boolean dropItem;
    private Integer despawnTimer;
    private Location targetLocation;
    private ItemStack item;

    public EntityEnderSignalData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        if (parameters.contains("drop_item"))
            dropItem = parameters.getBoolean("drop_item");
        if (parameters.contains("despawn_timer"))
            despawnTimer = parameters.getInt("despawn_timer");
        String itemKey = parameters.getString("item");
        if (itemKey != null && !itemKey.isEmpty()) {
            ItemData itemData = controller.getOrCreateItem(itemKey);
            if (itemData == null) {
                controller.getLogger().warning("Invalid item in ender_signal.item config: " + itemKey);
            } else {
                item = itemData.getItemStack();
            }
        }
        // TODO: Location
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
            }
            if (item != null) {
                signal.setItem(item);
            }
        }
    }
}
