package com.elmakers.mine.bukkit.entity;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class EntityChestedHorseData extends EntityAbstractHorseData {
    private boolean hasChest;
    private ItemStack[] inventory;

    public EntityChestedHorseData() {

    }

    public EntityChestedHorseData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        hasChest = parameters.getBoolean("has_chest", false);
        List<String> inventory = ConfigurationUtils.getStringList(parameters, "inventory");
        if (inventory != null && !inventory.isEmpty()) {
            this.inventory = new ItemStack[inventory.size()];
            int index = 0;
            for (String itemKey : inventory) {
                ItemData data = controller.getOrCreateItemOrWand(itemKey);
                this.inventory[index++] = data == null ? null : data.getItemStack();
            }
        }
    }

    public EntityChestedHorseData(Entity entity) {
        super(entity);
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
            horse.setCarryingChest(hasChest);
            Inventory inventory = horse.getInventory();
            if (inventory != null) {
                inventory.clear();
                if (this.inventory != null) {
                    inventory.setContents(this.inventory);
                }
            }
        }
    }
}
