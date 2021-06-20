package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;

import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class EntityZombieData extends EntityExtraData {
    public boolean isAdult;

    public EntityZombieData() {
    }

    public EntityZombieData(Zombie zombie) {
       isAdult = CompatibilityLib.getCompatibilityUtils().isAdult(zombie);
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Zombie)) return;
        Zombie zombie = (Zombie)entity;
        if (isAdult) {
            CompatibilityLib.getCompatibilityUtils().setAdult(zombie);
        } else {
            CompatibilityLib.getCompatibilityUtils().setBaby(zombie);
        }
    }
}
