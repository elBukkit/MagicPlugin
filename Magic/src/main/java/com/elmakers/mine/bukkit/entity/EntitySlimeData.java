package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;

public class EntitySlimeData extends EntityExtraData {
    public int size;
    public boolean splittable;

    public EntitySlimeData() {

    }

    public EntitySlimeData(Slime slime) {
       size = slime.getSize();
       splittable = true;
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Slime)) return;
        Slime slime = (Slime)entity;
        slime.setSize(size);
    }
}
