package com.elmakers.mine.bukkit.entity;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wolf;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityWolfData extends EntityAnimalData {
    private boolean isAngry;
    private DyeColor collarColor;

    public EntityWolfData() {

    }

    public EntityWolfData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);

        String colorString = parameters.getString("color");
        if (colorString != null) {
            try {
                collarColor = DyeColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                collarColor = null;
            }
        }
        isAngry = parameters.getBoolean("angry", false);
    }

    public EntityWolfData(Entity entity) {
        super(entity);
        if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            collarColor = wolf.getCollarColor();
            isAngry = wolf.isAngry();
            sitting = wolf.isSitting();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            wolf.setCollarColor(collarColor);
            wolf.setAngry(isAngry);
            wolf.setSitting(sitting);
        }
    }
}
