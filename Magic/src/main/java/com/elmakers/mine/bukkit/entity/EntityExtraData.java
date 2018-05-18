package com.elmakers.mine.bukkit.entity;

import org.bukkit.entity.Entity;

public abstract class EntityExtraData implements Cloneable {
    @Override
    public abstract EntityExtraData clone();
    public abstract void apply(Entity entity);
}
