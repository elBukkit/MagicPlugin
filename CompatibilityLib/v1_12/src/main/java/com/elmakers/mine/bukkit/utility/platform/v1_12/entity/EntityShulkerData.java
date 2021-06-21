package com.elmakers.mine.bukkit.utility.platform.v1_12.entity;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;

import com.elmakers.mine.bukkit.entity.EntityExtraData;

public class EntityShulkerData extends EntityExtraData {
    private DyeColor color;

    public EntityShulkerData() {

    }

    public EntityShulkerData(ConfigurationSection parameters) {
        String colorString = parameters.getString("color");
        if (colorString != null) {
            try {
                color = DyeColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                color = null;
            }
        }
    }

    public EntityShulkerData(Entity entity) {
        if (entity instanceof Shulker) {
            Shulker shulker = (Shulker)entity;
            color = shulker.getColor();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof Shulker) {
            Shulker shulker = (Shulker)entity;
            if (color != null) {
                shulker.setColor(color);
            }
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }
        Shulker shulker = (Shulker)entity;
        DyeColor dyeColor = shulker.getColor();
        DyeColor[] dyeColorValues = DyeColor.values();
        dyeColor = dyeColorValues[(dyeColor.ordinal() + 1) % dyeColorValues.length];
        shulker.setColor(dyeColor);
        return true;
    }

    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Shulker;
    }
}
