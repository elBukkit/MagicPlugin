package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class EntityPhantomData extends EntityExtraData {
    public int size;

    public EntityPhantomData() {

    }

    public EntityPhantomData(Entity entity) {
       size = CompatibilityLib.getCompatibilityUtils().getPhantomSize(entity);
    }

    @Override
    public void apply(Entity entity) {
        CompatibilityLib.getCompatibilityUtils().setPhantomSize(entity, size);
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
