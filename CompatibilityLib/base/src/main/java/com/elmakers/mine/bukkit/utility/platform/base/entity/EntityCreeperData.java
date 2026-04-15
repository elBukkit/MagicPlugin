package com.elmakers.mine.bukkit.utility.platform.base.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class EntityCreeperData extends EntityExtraData {
    private Boolean powered;

    public EntityCreeperData(ConfigurationSection parameters) {
        powered = ConfigUtils.getOptionalBoolean(parameters, "powered");
    }

    public EntityCreeperData(Entity entity) {
        if (entity instanceof Creeper) {
            powered = ((Creeper)entity).isPowered();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof Creeper && powered != null) {
            ((Creeper)entity).setPowered(powered);
        }
    }
}
