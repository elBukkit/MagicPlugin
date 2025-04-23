package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Goat;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityGoatData extends EntityAnimalData {
    public Boolean screaming;

    public EntityGoatData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        screaming = ConfigUtils.getOptionalBoolean(parameters, "screaming");
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
        if (entity instanceof Goat && screaming != null) {
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
