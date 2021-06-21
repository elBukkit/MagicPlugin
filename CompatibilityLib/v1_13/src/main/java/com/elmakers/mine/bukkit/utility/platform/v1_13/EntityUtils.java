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
        switch (entity.getType()) {
            case FALLING_BLOCK:
                // Falling blocks overridden here to use BlockData
                return new EntityFallingBlockData(entity, controller);
            default:
                return super.getExtraData(controller, entity);
        }
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
