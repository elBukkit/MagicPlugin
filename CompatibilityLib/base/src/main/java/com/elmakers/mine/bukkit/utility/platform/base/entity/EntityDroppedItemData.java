package com.elmakers.mine.bukkit.utility.platform.base.entity;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.entity.SpawnedEntityExtraData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.platform.PlatformInterpreter;

public class EntityDroppedItemData extends EntityExtraData {
    protected ItemStack item;
    protected Integer pickupDelay;

    public EntityDroppedItemData(ConfigurationSection parameters, MageController controller) {
        Logger log = controller.getLogger();
        String itemKey = parameters.getString("item");
        if (itemKey != null && !itemKey.isEmpty()) {
            ItemData itemData = controller.getOrCreateItem(itemKey);
            if (itemData == null) {
                log.warning("Invalid item in dropped item config: " + itemKey);
            } else {
                item = itemData.getItemStack();
            }
        }
        if (ConfigUtils.isMaxValue(parameters.getString("pickup_delay"))) {
            pickupDelay = Integer.MAX_VALUE;
        } else {
            pickupDelay = ConfigUtils.getOptionalInteger(parameters, "pickup_delay");
        }
    }

    public EntityDroppedItemData(Entity entity) {
        if (entity instanceof Item) {
            Item droppedItem = (Item)entity;
            item = PlatformInterpreter.getPlatform().getItemUtils().getCopy(droppedItem.getItemStack());
            pickupDelay = droppedItem.getPickupDelay();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof Item) {
            Item droppedItem = (Item)entity;
            if (!PlatformInterpreter.getPlatform().getItemUtils().isEmpty(item)) {
                droppedItem.setItemStack(item);
            }
            if (pickupDelay != null) {
                droppedItem.setPickupDelay(pickupDelay);
            }
        }
    }

    @Override
    public SpawnedEntityExtraData spawn(Location location) {
        Entity newEntity = null;
        if (!PlatformInterpreter.getPlatform().getItemUtils().isEmpty(item)) {
            newEntity = location.getWorld().dropItem(location, item);
        }
        if (newEntity != null && pickupDelay != null && newEntity instanceof Item) {
            ((Item)newEntity).setPickupDelay(pickupDelay);
        }
        return new SpawnedEntityExtraData(newEntity, true);
    }
}
