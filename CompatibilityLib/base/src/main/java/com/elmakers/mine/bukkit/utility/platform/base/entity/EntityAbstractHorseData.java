package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityAbstractHorseData extends EntityAnimalData {
    public Integer domestication;
    public Integer maxDomestication;
    public Double jumpStrength;

    public EntityAbstractHorseData() {

    }

    public EntityAbstractHorseData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);

        if (parameters.contains("horse_jump_strength")) {
            jumpStrength = parameters.getDouble("horse_jump_strength");
        }
        if (parameters.contains("jump_strength")) {
            jumpStrength = parameters.getDouble("jump_strength");
        }
        if (parameters.contains("domestication")) {
            domestication = parameters.getInt("domestication");
        }
        if (parameters.contains("max_domestication")) {
            maxDomestication = parameters.getInt("max_domestication");
        }
    }

    public EntityAbstractHorseData(Entity entity) {
        super(entity);
    }
}
