package com.elmakers.mine.bukkit.utility.platform.base.entity;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MushroomCow;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityMooshroomData extends EntityAnimalData {
    public MushroomCow.Variant variant;

    public EntityMooshroomData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        Logger log = controller.getLogger();
        String variantName = parameters.getString("mushroom_cow_variant");
        if (variantName != null && !variantName.isEmpty()) {
            try {
                variant = MushroomCow.Variant.valueOf(variantName.toUpperCase());
            } catch (Exception ex) {
                log.warning("Invalid mooshroom variant: " + variantName);
            }
        }
    }

    public EntityMooshroomData(Entity entity) {
        super(entity);
        if (entity instanceof MushroomCow) {
            MushroomCow cow = (MushroomCow)entity;
            variant = cow.getVariant();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof MushroomCow) {
            MushroomCow cow = (MushroomCow)entity;
            if (variant != null) {
                cow.setVariant(variant);
            }
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }
        MushroomCow cow = (MushroomCow)entity;
        MushroomCow.Variant type = cow.getVariant();
        MushroomCow.Variant[] typeValues = MushroomCow.Variant.values();
        int typeOrdinal = (type.ordinal() + 1) % typeValues.length;
        type = typeValues[typeOrdinal];
        cow.setVariant(type);
        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof MushroomCow;
    }
}
