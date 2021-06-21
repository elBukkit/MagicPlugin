package com.elmakers.mine.bukkit.utility.platform.base.entity;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Rabbit;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class EntityRabbitData extends EntityAnimalData {
    private Rabbit.Type type;

    public EntityRabbitData() {

    }

    public EntityRabbitData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        Logger log = controller.getLogger();
        String typeString = parameters.getString("rabbit_type");
        if (typeString != null && !typeString.isEmpty()) {
            try {
                type = Rabbit.Type.valueOf(typeString.toUpperCase());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Invalid rabbit_type: " + typeString, ex);
            }
        }
    }

    public EntityRabbitData(Entity entity) {
        super(entity);
        if (entity instanceof Rabbit) {
            Rabbit rabbit = (Rabbit)entity;
            type = rabbit.getRabbitType();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Rabbit) {
            Rabbit rabbit = (Rabbit)entity;
            if (type != null) {
                rabbit.setRabbitType(type);
            }
        }
    }
}
