package com.elmakers.mine.bukkit.utility.platform.v1_14.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;

import com.elmakers.mine.bukkit.entity.EntityExtraData;

public class EntityPhantomData extends EntityExtraData {
    public int size;

    public EntityPhantomData(ConfigurationSection parameters) {
        size = parameters.getInt("size", 1);
    }

    public EntityPhantomData(Entity entity) {
        if (entity instanceof Phantom) {
            size = ((Phantom)entity).getSize();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof Phantom) {
            ((Phantom)entity).setSize(size);
        }
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
