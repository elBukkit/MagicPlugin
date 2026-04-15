package com.elmakers.mine.bukkit.utility.platform.v1_12.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityAbstractHorseData extends com.elmakers.mine.bukkit.utility.platform.v1_11.entity.EntityAbstractHorseData {
    public ItemData saddle;
    protected boolean temporarySaddle;

    public EntityAbstractHorseData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        saddle = controller.getOrCreateItem(parameters.getString("saddle"));
        temporarySaddle = parameters.getBoolean("saddle_temporary");
    }

    public EntityAbstractHorseData(Entity entity, MageController controller) {
        super(entity);
        if (entity instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse)entity;
            saddle = getItem(horse.getInventory().getSaddle(), controller);
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse)entity;
            if (saddle != null) {
                ItemStack saddleItem = saddle.getItemStack(1);
                if (temporarySaddle) {
                    saddleItem = getPlatform().getItemUtils().makeReal(saddleItem);
                    getPlatform().getItemUtils().makeTemporary(saddleItem, "");
                }
                horse.getInventory().setSaddle(saddleItem);
            }
        }
    }
}
