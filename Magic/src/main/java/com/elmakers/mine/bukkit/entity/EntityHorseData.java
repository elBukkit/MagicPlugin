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

public class EntityHorseData extends EntityExtraData {
    public Horse.Color color;
    public Horse.Style style;
    public ItemData saddle;
    public ItemData armor;
    public Integer domestication;
    public Integer maxDomestication;
    public Double jumpStrength;
    public Boolean tamed;

    public EntityHorseData() {

    }

    public EntityHorseData(ConfigurationSection parameters, MageController controller) {
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

        if (parameters.contains("tamed")) {
            tamed = parameters.getBoolean("tamed");
        }

        saddle = controller.getOrCreateItemOrWand(parameters.getString("saddle"));
        armor = controller.getOrCreateItemOrWand(parameters.getString("armor"));
    }

    public EntityHorseData(Horse horse) {
        color = horse.getColor();
        style = horse.getStyle();
        saddle = getItem(horse.getInventory().getSaddle());
        armor = getItem(horse.getInventory().getArmor());
        domestication = horse.getDomestication();
        maxDomestication = horse.getMaxDomestication();
        jumpStrength = horse.getJumpStrength();
        tamed = horse.isTamed();
    }

    @Nullable
    private ItemData getItem(ItemStack item) {
        return item == null ? null : new com.elmakers.mine.bukkit.item.ItemData(item);
    }

    @Override
    public void apply(Entity entity) {
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
        if (domestication != null) {
            horse.setDomestication(domestication);
        }
        if (maxDomestication != null) {
            horse.setMaxDomestication(maxDomestication);
        }
        if (jumpStrength != null) {
            horse.setJumpStrength(jumpStrength);
        }
        if (tamed != null) {
            horse.setTamed(tamed);
        }
    }
}
