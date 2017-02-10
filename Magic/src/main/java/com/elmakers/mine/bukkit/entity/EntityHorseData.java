package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;

public class EntityHorseData extends EntityExtraData {
    public Horse.Color color;
    public Horse.Style style;
    public ItemStack saddle;
    public ItemStack armor;
    public Integer domestication;
    public Integer maxDomestication;
    public Double jumpStrength;
    public Boolean tamed;

    public EntityHorseData() {

    }

    public EntityHorseData(Horse horse) {
        color = horse.getColor();
        style = horse.getStyle();
        saddle = horse.getInventory().getSaddle();
        armor = horse.getInventory().getArmor();
        domestication = horse.getDomestication();
        maxDomestication = horse.getMaxDomestication();
        jumpStrength = horse.getJumpStrength();
        tamed = horse.isTamed();
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Horse)) return;
        Horse horse = (Horse)entity;

        horse.getInventory().setSaddle(saddle);
        horse.getInventory().setArmor(armor);
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

    @Override
    public EntityExtraData clone() {
        EntityHorseData copy = new EntityHorseData();
        copy.saddle = saddle == null ? null : saddle.clone();
        copy.armor = armor == null ? null : armor.clone();
        copy.color = color;
        copy.domestication = domestication;
        copy.style = style;
        copy.maxDomestication = maxDomestication;
        copy.jumpStrength = jumpStrength;
        copy.tamed = tamed;
        return copy;
    }

    @Override
    public void removed(Entity entity) {
    }
}
