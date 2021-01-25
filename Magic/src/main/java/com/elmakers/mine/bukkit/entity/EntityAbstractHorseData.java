package com.elmakers.mine.bukkit.entity;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityAbstractHorseData extends EntityAnimalData {
    public Integer domestication;
    public Integer maxDomestication;
    public Double jumpStrength;
    public ItemData saddle;

    public EntityAbstractHorseData() {

    }

    public EntityAbstractHorseData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);

        if (parameters.contains("horse_jump_strength")) {
            jumpStrength = parameters.getDouble("horse_jump_strength");
        }
        if (parameters.contains("jump_strength")) {
            jumpStrength = parameters.getDouble("jump_strength");
        }
        if (parameters.contains("domestication")) {
            domestication = parameters.getInt("domestication");
        }
        if (parameters.contains("max_domestication")) {
            maxDomestication = parameters.getInt("max_domestication");
        }

        saddle = controller.getOrCreateItem(parameters.getString("saddle"));
    }

    public EntityAbstractHorseData(Entity entity, MageController controller) {
        super(entity);
        if (entity instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse)entity;
            domestication = horse.getDomestication();
            maxDomestication = horse.getMaxDomestication();
            jumpStrength = horse.getJumpStrength();
            saddle = getItem(horse.getInventory().getSaddle(), controller);
        }
    }

    @Nullable
    protected ItemData getItem(ItemStack item, MageController controller) {
        return item == null ? null : controller.createItemData(item);
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse)entity;
            if (domestication != null) {
                horse.setDomestication(domestication);
            }
            if (maxDomestication != null) {
                horse.setMaxDomestication(maxDomestication);
            }
            if (jumpStrength != null) {
                horse.setJumpStrength(jumpStrength);
            }
            if (saddle != null) {
                horse.getInventory().setSaddle(saddle.getItemStack(1));
            }
        }
    }
}
