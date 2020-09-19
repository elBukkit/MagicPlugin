package com.elmakers.mine.bukkit.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class EntityFoxData extends EntityExtraData {
    public Object type;

    public EntityFoxData() {

    }

    public EntityFoxData(ConfigurationSection parameters, MageController controller) {
        Logger log = controller.getLogger();
        String typeString = parameters.getString("fox_type");
        if (typeString == null && !typeString.isEmpty()) {
            try {
                type = CompatibilityUtils.getFoxType(typeString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid fox_type: " + parameters.getString("fox_type"), ex);
            }
        }
    }

    public EntityFoxData(Entity fox) {
        if (CompatibilityUtils.isFox(fox)) {
            type = CompatibilityUtils.getFoxType(fox);
        }
    }

    @Override
    public void apply(Entity entity) {
        if (CompatibilityUtils.isFox(entity) && type != null) {
            CompatibilityUtils.setFoxType(entity, type);
        }
    }

    @Override
    public EntityExtraData clone() {
        EntityFoxData copy = new EntityFoxData();
        copy.type = type;
        return copy;
    }
}
