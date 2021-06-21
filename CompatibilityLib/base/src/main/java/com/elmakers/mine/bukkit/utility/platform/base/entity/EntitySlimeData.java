package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;

import com.elmakers.mine.bukkit.entity.EntityExtraData;

public class EntitySlimeData extends EntityExtraData {
    public int size;
    public boolean splittable;

    public EntitySlimeData(ConfigurationSection parameters) {
        size = parameters.getInt("size", 16);
        splittable = parameters.getBoolean("split", true);
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

    @Override
    public boolean isSplittable() {
        return splittable;
    }
}
