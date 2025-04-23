package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;

import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityPhantomData extends EntityExtraData {
    public Integer size;

    public EntityPhantomData(ConfigurationSection parameters) {
        size = ConfigUtils.getOptionalInteger(parameters, "size");
    }

    public EntityPhantomData(Entity entity) {
        if (entity instanceof Phantom) {
            size = ((Phantom)entity).getSize();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof Phantom && size != null) {
            ((Phantom)entity).setSize(size);
        }
    }
}
