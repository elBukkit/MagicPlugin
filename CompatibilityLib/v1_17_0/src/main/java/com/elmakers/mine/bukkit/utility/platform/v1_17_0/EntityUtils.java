package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.v1_17_0.entity.EntityGoatData;

public class EntityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_16.EntityUtils  {
    public EntityUtils(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        EntityExtraData extraData = null;
        if (entity.getType() == EntityType.GOAT) {
            extraData = new EntityGoatData(entity);
        }
        if (extraData == null) {
            extraData = super.getExtraData(controller, entity);
        }
        return extraData;
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case GOAT:
                return new EntityGoatData(parameters, controller);
            case FOX:
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
