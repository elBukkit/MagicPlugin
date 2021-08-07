package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;

import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntitySlimeData extends EntityExtraData {
    public Integer size;
    public boolean splittable;

    public EntitySlimeData(ConfigurationSection parameters) {
        size = ConfigUtils.getOptionalInteger(parameters, "size");
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
        if (size != null) slime.setSize(size);
    }

    @Override
    public boolean isSplittable() {
        return splittable;
    }
}
