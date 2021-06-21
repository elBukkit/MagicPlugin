package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntitySheepData extends EntityAnimalData {
    private DyeColor color;
    private boolean sheared;

    public EntitySheepData() {

    }

    public EntitySheepData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);

        String colorString = parameters.getString("color");
        if (colorString != null) {
            try {
                color = DyeColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                color = null;
            }
        }
        sheared = parameters.getBoolean("sheared");
    }

    public EntitySheepData(Entity entity) {
        super(entity);
        if (entity instanceof Sheep) {
            Sheep sheep = (Sheep)entity;
            color = sheep.getColor();
            sheared = sheep.isSheared();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Sheep) {
            Sheep sheep = (Sheep)entity;
            if (color != null) {
                sheep.setColor(color);
            }
            sheep.setSheared(sheared);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }
        Sheep sheep = (Sheep)entity;
        DyeColor dyeColor = sheep.getColor();
        DyeColor[] dyeColorValues = DyeColor.values();
        dyeColor = dyeColorValues[(dyeColor.ordinal() + 1) % dyeColorValues.length];
        sheep.setColor(dyeColor);
        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Sheep;
    }
}
