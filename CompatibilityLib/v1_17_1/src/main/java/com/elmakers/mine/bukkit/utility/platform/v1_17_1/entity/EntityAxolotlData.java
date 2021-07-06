package com.elmakers.mine.bukkit.utility.platform.v1_17_1.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAnimalData;

public class EntityAxolotlData extends EntityAnimalData {
    public Axolotl.Variant variant;

    public EntityAxolotlData() {

    }

    public EntityAxolotlData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        Logger log = controller.getLogger();
        String typeString = parameters.getString("axolotl_type");
        if (typeString != null && !typeString.isEmpty()) {
            try {
                variant = Axolotl.Variant.valueOf(typeString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid axolotl_type: " + typeString, ex);
            }
        }
    }

    public EntityAxolotlData(Entity entity) {
        super(entity);
        if (entity instanceof Axolotl) {
            Axolotl axolotl = (Axolotl)entity;
            variant = axolotl.getVariant();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Axolotl) {
            Axolotl axolotl = (Axolotl)entity;
            if (variant != null) {
                axolotl.setVariant(variant);
            }
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }
        Axolotl axolotl = (Axolotl)entity;
        Axolotl.Variant type = axolotl.getVariant();
        Axolotl.Variant[] typeValues = Axolotl.Variant.values();
        int typeOrdinal = (type.ordinal() + 1) % typeValues.length;
        type = typeValues[typeOrdinal];
        axolotl.setVariant(type);
        return true;
    }

    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Axolotl;
    }
}
