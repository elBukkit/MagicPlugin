package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;

import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityZombieData extends EntityExtraData {
    public Boolean isBaby;

    public EntityZombieData(ConfigurationSection configuration) {
        isBaby = ConfigUtils.getOptionalBoolean(configuration, "baby");
    }

    public EntityZombieData(Zombie zombie) {
       isBaby = getPlatform().getCompatibilityUtils().isAdult(zombie);
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Zombie)) return;
        Zombie zombie = (Zombie)entity;
        if (isBaby != null) {
            if (isBaby) {
                getPlatform().getCompatibilityUtils().setBaby(zombie);
            } else {
                getPlatform().getCompatibilityUtils().setAdult(zombie);
            }
        }
    }
}
