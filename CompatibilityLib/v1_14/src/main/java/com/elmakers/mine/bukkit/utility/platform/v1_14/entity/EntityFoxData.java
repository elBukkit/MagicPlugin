package com.elmakers.mine.bukkit.utility.platform.v1_14.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAnimalData;

public class EntityFoxData extends EntityAnimalData {
    private Fox.Type type;
    private boolean crouching;

    public EntityFoxData() {

    }

    public EntityFoxData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        Logger log = controller.getLogger();
        String typeString = parameters.getString("fox_type");
        if (typeString != null && !typeString.isEmpty()) {
            try {
                type = Fox.Type.valueOf(typeString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid fox_type: " + typeString, ex);
            }
        }
        crouching = parameters.getBoolean("crouching");
    }

    public EntityFoxData(Entity entity) {
        super(entity);
        if (entity instanceof Fox) {
            Fox fox = (Fox)entity;
            type = fox.getFoxType();
            crouching = fox.isCrouching();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Fox) {
            Fox fox = (Fox)entity;
            if (type != null) {
                fox.setFoxType(type);
            }
            fox.setCrouching(crouching);
        }
    }
}
