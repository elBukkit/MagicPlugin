package com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Goat;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityGoatData extends EntityAnimalData {
    public Boolean screaming;
    public Boolean leftHorn;
    public Boolean rightHorn;

    public EntityGoatData(ConfigurationSection parameters, MageController controller) {
        super(parameters, controller);
        screaming = ConfigUtils.getOptionalBoolean(parameters, "screaming");
        leftHorn = ConfigUtils.getOptionalBoolean(parameters, "horn_left");
        rightHorn = ConfigUtils.getOptionalBoolean(parameters, "horn_right");
    }

    public EntityGoatData(Entity entity) {
        super(entity);
        if (entity instanceof Goat) {
            Goat goat = (Goat)entity;
            screaming = goat.isScreaming();
            rightHorn = goat.hasRightHorn();
            leftHorn = goat.hasLeftHorn();
        }
    }

    @Override
    public void apply(Entity entity) {
        super.apply(entity);
        if (entity instanceof Goat) {
            Goat goat = (Goat)entity;
            if (screaming != null) goat.setScreaming(screaming);
            if (rightHorn != null) goat.setScreaming(rightHorn);
            if (leftHorn != null) goat.setScreaming(leftHorn);
        }
    }

    @Override
    public boolean cycle(Entity entity) {
        if (!canCycle(entity)) {
            return false;
        }
        Goat goat = (Goat)entity;
        goat.setScreaming(!goat.isScreaming());
        if (goat.isScreaming()) {
            int hornMask = (goat.hasRightHorn() ? 1 : 0) | (goat.hasLeftHorn() ? 2 : 0);
            hornMask = (hornMask + 1) & 3;
            goat.setRightHorn((hornMask & 1) == 1);
            goat.setLeftHorn((hornMask & 2) == 2);
        }
        return true;
    }

    @Override
    public boolean canCycle(Entity entity) {
        return entity instanceof Goat;
    }
}
