package com.elmakers.mine.bukkit.utility.platform.v1_21_11.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Bogged;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAnimalData;

public class EntityBoggedData extends EntityAnimalData {
    private Boolean sheared;

    public EntityBoggedData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        sheared = ConfigUtils.getOptionalBoolean(parameters, "sheared");
    }

    public EntityBoggedData(Entity entity) {
        super(entity);
        if (entity instanceof Bogged) {
            Bogged bogged = (Bogged)entity;
            sheared = bogged.isSheared();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Bogged) {
            Bogged bogged = (Bogged)entity;
            if (sheared != null) bogged.setSheared(sheared);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }

        Bogged bogged = (Bogged)entity;
        bogged.setSheared(!bogged.isSheared());

        return true;
    }


    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Bogged;
    }
}
