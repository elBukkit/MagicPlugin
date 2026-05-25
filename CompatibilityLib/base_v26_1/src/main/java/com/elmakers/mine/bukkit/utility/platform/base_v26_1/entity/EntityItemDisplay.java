package com.elmakers.mine.bukkit.utility.platform.base_v26_1.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityItemDisplay extends EntityDisplay {
    protected ItemStack item;

    public EntityItemDisplay(ConfigurationSection configuration, MageController controller) {
        super(configuration, controller);

        String itemKey = configuration.getString("item");
        if (itemKey != null && !itemKey.isEmpty()) {
            ItemData itemData = controller.getOrCreateItem(itemKey);
            if (itemData == null) {
                controller.getLogger().warning("Invalid item in item display config: " + itemKey);
            } else {
                item = itemData.getItemStack();
            }
        }
    }

    public EntityItemDisplay(Entity entity, MageController controller) {
        super(entity, controller);
        if (entity instanceof ItemDisplay) {
            ItemDisplay display = (ItemDisplay) entity;
            item = getPlatform().getItemUtils().getCopy(display.getItemStack());
        }
    }

    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof ItemDisplay) {
            ItemDisplay display = (ItemDisplay) entity;
            if (!getPlatform().getItemUtils().isEmpty(item)) {
                display.setItemStack(item);
            }
        }
    }
}
