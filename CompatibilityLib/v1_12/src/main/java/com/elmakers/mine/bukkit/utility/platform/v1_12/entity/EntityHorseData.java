package com.elmakers.mine.bukkit.utility.platform.v1_12.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityHorseData extends EntityAbstractHorseData {
    public Horse.Color color;
    public Horse.Style style;
    public ItemData armor;
    protected boolean temporaryArmor;

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
        temporaryArmor = parameters.getBoolean("armor_temporary");
    }

    public EntityHorseData(Entity entity, MageController controller) {
        super(entity, controller);
        if (entity instanceof Horse) {
            Horse horse = (Horse)entity;
            color = horse.getColor();
            style = horse.getStyle();
            armor = getItem(horse.getInventory().getArmor(), controller);
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (!(entity instanceof Horse)) return;
        Horse horse = (Horse)entity;

        if (armor != null) {
            ItemStack armorItem = armor.getItemStack(1);
            if (temporaryArmor) {
                armorItem = getPlatform().getItemUtils().makeReal(armorItem);
                getPlatform().getItemUtils().makeTemporary(armorItem, "");
            }
            horse.getInventory().setArmor(armorItem);
        }
        if (color != null) {
            horse.setColor(color);
        }
        if (style != null) {
            horse.setStyle(style);
        }
    }
}
