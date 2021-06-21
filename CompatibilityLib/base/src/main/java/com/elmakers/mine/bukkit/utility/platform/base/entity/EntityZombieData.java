package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;

import com.elmakers.mine.bukkit.entity.EntityExtraData;

public class EntityZombieData extends EntityExtraData {
    public boolean isAdult;

    public EntityZombieData(ConfigurationSection configuration) {
        isAdult = !configuration.getBoolean("baby");
    }

    public EntityZombieData(Zombie zombie) {
       isAdult = getPlatform().getCompatibilityUtils().isAdult(zombie);
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Zombie)) return;
        Zombie zombie = (Zombie)entity;
        if (isAdult) {
            getPlatform().getCompatibilityUtils().setAdult(zombie);
        } else {
            getPlatform().getCompatibilityUtils().setBaby(zombie);
        }
    }
}
