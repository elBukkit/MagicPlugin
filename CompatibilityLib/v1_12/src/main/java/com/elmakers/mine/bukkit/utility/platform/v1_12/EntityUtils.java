package com.elmakers.mine.bukkit.utility.platform.v1_12;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.v1_12.entity.EntityParrotData;

public class EntityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_11.EntityUtils  {
    public EntityUtils(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        EntityExtraData extraData = null;
        if (entity.getType() == EntityType.PARROT) {
            extraData = new EntityParrotData(entity);
        }
        if (extraData == null) {
            extraData = super.getExtraData(controller, entity);
        }
        return extraData;
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case PARROT:
                return new EntityParrotData(parameters, controller);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
