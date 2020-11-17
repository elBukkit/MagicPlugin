package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class EntityPhantomData extends EntityExtraData {
    public int size;

    public EntityPhantomData() {

    }

    public EntityPhantomData(Entity entity) {
       size = CompatibilityUtils.getPhantomSize(entity);
    }

    @Override
    public void apply(Entity entity) {
        CompatibilityUtils.setPhantomSize(entity, size);
    }
}
