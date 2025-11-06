package com.elmakers.mine.bukkit.utility.platform.base.entity;

import java.util.Locale;

import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wolf;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityWolfData extends EntityAnimalData {
    private Boolean isAngry;
    private DyeColor collarColor;
    private Wolf.Variant variant;

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
        isAngry = ConfigUtils.getOptionalBoolean(parameters, "angry");

        String variantString = parameters.getString("variant");
        if (variantString != null) {
            NamespacedKey namespacedKey = NamespacedKey.minecraft(variantString.toLowerCase(Locale.ROOT));
            variant = Registry.WOLF_VARIANT.get(namespacedKey);
        }
    }

    public EntityWolfData(Entity entity) {
        super(entity);
        if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            collarColor = wolf.getCollarColor();
            isAngry = wolf.isAngry();
            sitting = wolf.isSitting();
            variant = wolf.getVariant();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            if (collarColor != null) {
                wolf.setCollarColor(collarColor);
            }
            if (isAngry != null) wolf.setAngry(isAngry);
            if (sitting != null) wolf.setSitting(sitting);
            if (variant != null) wolf.setVariant(variant);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }

        Wolf wolf = (Wolf)entity;
        DyeColor wolfColor = wolf.getCollarColor();
        DyeColor[] wolfColorValues = DyeColor.values();
        wolfColor = wolfColorValues[(wolfColor.ordinal() + 1) % wolfColorValues.length];
        wolf.setCollarColor(wolfColor);
        wolf.setVariant(cycleRegistryValue(wolf.getVariant(), Registry.WOLF_VARIANT));

        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Wolf;
    }
}
