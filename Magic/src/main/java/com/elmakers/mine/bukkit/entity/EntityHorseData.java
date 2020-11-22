package com.elmakers.mine.bukkit.entity;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityHorseData extends EntityAbstractHorseData {
    public Horse.Color color;
    public Horse.Style style;
    public ItemData saddle;
    public ItemData armor;

    public EntityHorseData() {

    }

    public EntityHorseData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        Logger log = controller.getLogger();
        if (parameters.contains("horse_color")) {
            try {
                String colorString = parameters.getString("horse_color");
                color = Horse.Color.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid horse_color: " + parameters.getString("horse_color"), ex);
            }
        }

        if (parameters.contains("horse_style")) {
            try {
                String styleString = parameters.getString("horse_style");
                style = Horse.Style.valueOf(styleString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid horse_style: " + parameters.getString("horse_style"), ex);
            }
        }

        if (parameters.contains("horse_jump_strength")) {
            jumpStrength = parameters.getDouble("horse_jump_strength");
        }
    }

    public EntityHorseData(Horse horse) {
        super(horse);
        color = horse.getColor();
        style = horse.getStyle();
        saddle = getItem(horse.getInventory().getSaddle());
        armor = getItem(horse.getInventory().getArmor());
    }

    @Nullable
    private ItemData getItem(ItemStack item) {
        return item == null ? null : new com.elmakers.mine.bukkit.item.ItemData(item);
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (!(entity instanceof Horse)) return;
        Horse horse = (Horse)entity;

        horse.getInventory().setSaddle(saddle == null ? null : saddle.getItemStack(1));
        horse.getInventory().setArmor(armor == null ? null : armor.getItemStack(1));
        if (color != null) {
            horse.setColor(color);
        }
        if (style != null) {
            horse.setStyle(style);
        }
    }
}
