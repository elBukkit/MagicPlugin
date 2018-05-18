package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;

public class EntitySlimeData extends EntityExtraData {
    public int size;

    public EntitySlimeData() {

    }

    public EntitySlimeData(Slime slime) {
       size = slime.getSize();
    }

    @Override
    public void apply(Entity entity) {
        if (!(entity instanceof Slime)) return;
        Slime slime = (Slime)entity;
        slime.setSize(size);
    }

    @Override
    public EntityExtraData clone() {
        EntitySlimeData copy = new EntitySlimeData();
        copy.size = size;
        return copy;
    }
}
