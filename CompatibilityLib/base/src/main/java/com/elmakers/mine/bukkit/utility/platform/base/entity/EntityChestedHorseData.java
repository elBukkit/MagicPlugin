package com.elmakers.mine.bukkit.utility.platform.base.entity;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityChestedHorseData extends EntityAbstractHorseData {
    private Boolean hasChest;
    private ItemStack[] inventory;

    public EntityChestedHorseData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        hasChest = ConfigUtils.getOptionalBoolean(parameters, "has_chest");
        List<String> inventory = ConfigUtils.getStringList(parameters, "inventory");
        if (inventory != null && !inventory.isEmpty()) {
            this.inventory = new ItemStack[inventory.size()];
            int index = 0;
            for (String itemKey : inventory) {
                ItemData data = controller.getOrCreateItem(itemKey);
                this.inventory[index++] = data == null ? null : data.getItemStack();
            }
        }
    }

    public EntityChestedHorseData(Entity entity, MageController controller) {
        super(entity, controller);
        if (entity instanceof ChestedHorse) {
            ChestedHorse horse = (ChestedHorse)entity;
            hasChest = horse.isCarryingChest();
            Inventory inventory = horse.getInventory();
            if (inventory != null) {
                this.inventory = inventory.getContents();
            }
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof ChestedHorse) {
            ChestedHorse horse = (ChestedHorse)entity;
            if (hasChest != null) horse.setCarryingChest(hasChest);
            if (this.inventory != null) {
                Inventory inventory = horse.getInventory();
                if (inventory != null) {
                    inventory.clear();
                    inventory.setContents(this.inventory);
                }
            }
        }
    }
}
