package com.elmakers.mine.bukkit.utility.platform.v1_14.entity;

import java.util.logging.Logger;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAnimalData;

public class EntityCatData extends EntityAnimalData {
    private Cat.Type type;
    private DyeColor collarColor;

    public EntityCatData() {

    }

    public EntityCatData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);

        Logger log = controller.getLogger();
        String typeName = parameters.getString("cat_type");
        if (typeName != null && !typeName.isEmpty()) {
            try {
                type = Cat.Type.valueOf(typeName.toUpperCase());
            } catch (Exception ex) {
                log.warning("Invalid cat type: " + typeName);
            }
        }

        String colorString = parameters.getString("color");
        if (colorString != null && !colorString.isEmpty()) {
            try {
                collarColor = DyeColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                log.warning("Invalid collar color: " + colorString);
                collarColor = null;
            }
        }
    }

    public EntityCatData(Entity entity) {
        super(entity);
        if (entity instanceof Cat) {
            Cat cat = (Cat)entity;
            type = cat.getCatType();
            collarColor = cat.getCollarColor();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Cat) {
            Cat cat = (Cat)entity;
            if (type != null) {
                cat.setCatType(type);
            }
            if (collarColor != null) {
                cat.setCollarColor(collarColor);
            }
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }

        Cat cat = (Cat)entity;
        Cat.Type catType = cat.getCatType();
        Cat.Type[] typeValues = Cat.Type.values();
        catType = typeValues[(catType.ordinal() + 1) % typeValues.length];
        cat.setCatType(catType);
        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Cat;
    }
}
