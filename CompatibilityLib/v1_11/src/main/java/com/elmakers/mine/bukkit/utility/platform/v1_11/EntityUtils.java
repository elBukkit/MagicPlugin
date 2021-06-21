package com.elmakers.mine.bukkit.utility.platform.v1_11;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.v1_11.entity.EntityLlamaData;
import com.elmakers.mine.bukkit.utility.platform.v1_11.entity.EntityMuleData;

public class EntityUtils extends com.elmakers.mine.bukkit.utility.platform.legacy.EntityUtils  {
    public EntityUtils(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        EntityExtraData extraData = null;
        if (entity.getType() == EntityType.LLAMA) {
            extraData = new EntityLlamaData(entity, controller);
        }
        if (entity.getType() == EntityType.MULE) {
            extraData = new EntityMuleData(entity, controller);
        }
        if (extraData == null) {
            extraData = super.getExtraData(controller, entity);
        }
        return extraData;
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case MULE:
                return new EntityMuleData(parameters, controller);
            case LLAMA:
                return new EntityLlamaData(parameters, controller);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
