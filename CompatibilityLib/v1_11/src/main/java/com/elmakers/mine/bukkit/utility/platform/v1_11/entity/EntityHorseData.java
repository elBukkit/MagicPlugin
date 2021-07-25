package com.elmakers.mine.bukkit.utility.platform.v1_11.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityHorseData extends EntityAbstractHorseData {
    public Horse.Color color;
    public Horse.Style style;
    public ItemData armor;
    public ItemData saddle;

    public EntityHorseData() {

    }

    public EntityHorseData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        Logger log = controller.getLogger();
        String horseColorKey = parameters.getString("horse_color");
        if (horseColorKey != null && !horseColorKey.isEmpty()) {
            try {
                color = Horse.Color.valueOf(horseColorKey.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid horse_color: " + horseColorKey);
            }
        }

        String styleString = parameters.getString("horse_style");
        if (styleString != null && !styleString.isEmpty()) {
            try {
                style = Horse.Style.valueOf(styleString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid horse_style: " + parameters.getString("horse_style"));
            }
        }

        if (parameters.contains("horse_jump_strength")) {
            jumpStrength = parameters.getDouble("horse_jump_strength");
        }
        armor = controller.getOrCreateItem(parameters.getString("armor"));
        saddle = controller.getOrCreateItem(parameters.getString("saddle"));
    }

    public EntityHorseData(Entity entity, MageController controller) {
        super(entity);
        if (entity instanceof Horse) {
            Horse horse = (Horse)entity;
            color = horse.getColor();
            style = horse.getStyle();
            armor = getItem(horse.getInventory().getArmor(), controller);
            saddle = getItem(horse.getInventory().getSaddle(), controller);
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (!(entity instanceof Horse)) return;
        Horse horse = (Horse)entity;

        if (armor != null) {
            horse.getInventory().setArmor(armor.getItemStack(1));
        }
        if (saddle != null) {
            horse.getInventory().setSaddle(armor.getItemStack(1));
        }
        if (color != null) {
            horse.setColor(color);
        }
        if (style != null) {
            horse.setStyle(style);
        }
    }
}
