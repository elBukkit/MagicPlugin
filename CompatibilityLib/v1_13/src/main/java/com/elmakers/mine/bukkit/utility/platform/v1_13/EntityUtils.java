package com.elmakers.mine.bukkit.utility.platform.v1_13;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.v1_13.entity.EntityFallingBlockData;

public class EntityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_12.EntityUtils  {
    public EntityUtils(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        EntityExtraData extraData = null;
        // Falling blocks overridden here to use BlockData
        if (entity.getType() == EntityType.FALLING_BLOCK) {
            extraData = new EntityFallingBlockData(entity, controller);
        }
        if (extraData == null) {
            return super.getExtraData(controller, entity);
        }
        return extraData;
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case FALLING_BLOCK:
                return new EntityFallingBlockData(parameters, controller);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
