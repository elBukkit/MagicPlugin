package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;

public class EntityZombieData extends EntityExtraData {
    public boolean isBaby;

    public EntityZombieData() {

    }

    public EntityZombieData(Zombie zombie) {
       isBaby = zombie.isBaby();
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Zombie)) return;
        Zombie zombie = (Zombie)entity;
        zombie.setBaby(isBaby);
    }
}
