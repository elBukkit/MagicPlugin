package com.elmakers.mine.bukkit.utility.platform.v1_17_0.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Goat;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAnimalData;

public class EntityGoatData extends EntityAnimalData {
    public boolean screaming;

    public EntityGoatData() {

    }

    public EntityGoatData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        screaming = parameters.getBoolean("screaming");
    }

    public EntityGoatData(Entity entity) {
        super(entity);
        if (entity instanceof Goat) {
            Goat goat = (Goat)entity;
            screaming = goat.isScreaming();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Goat) {
            Goat goat = (Goat)entity;
            goat.setScreaming(screaming);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }
        Goat goat = (Goat)entity;
        goat.setScreaming(!goat.isScreaming());
        return true;
    }

    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Goat;
    }
}
