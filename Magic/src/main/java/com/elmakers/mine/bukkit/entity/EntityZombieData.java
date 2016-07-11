package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;

public class EntityZombieData extends EntityExtraData {
    public boolean isBaby;
    public Villager.Profession profession;

    public EntityZombieData() {

    }

    public EntityZombieData(Zombie zombie) {
       isBaby = zombie.isBaby();
        profession = zombie.getVillagerProfession();
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Zombie)) return;
        Zombie zombie = (Zombie)entity;

        zombie.setBaby(isBaby);
        zombie.setVillagerProfession(profession);
    }

    @Override
    public EntityExtraData clone() {
        EntityZombieData copy = new EntityZombieData();
        copy.isBaby = isBaby;
        copy.profession = profession;
        return copy;
    }

    @Override
    public void removed(Entity entity) {
    }
}
