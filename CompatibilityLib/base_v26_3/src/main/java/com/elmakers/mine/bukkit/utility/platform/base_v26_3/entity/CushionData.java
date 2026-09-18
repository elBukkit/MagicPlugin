package com.elmakers.mine.bukkit.utility.platform.base_v26_3.entity;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Cushion;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.utility.platform.base_v26_1.entity.EntityExtraData;

public class CushionData extends EntityExtraData {
    public DyeColor color;

    public CushionData(ConfigurationSection parameters) {
        String colorString = parameters.getString("color");
        if (colorString != null) {
            try {
                color = DyeColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                color = null;
            }
        }
    }

    public CushionData(Entity entity) {
        if (entity instanceof Cushion cushion) {
            // color = cushion.getColor();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Cushion)) return;
        Cushion cushion = (Cushion)entity;
        // if (color != null) cushion.setColor(color);
    }
}
