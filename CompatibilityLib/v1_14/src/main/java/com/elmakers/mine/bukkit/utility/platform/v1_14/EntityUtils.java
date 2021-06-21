package com.elmakers.mine.bukkit.utility.platform.v1_14;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.v1_14.entity.EntityCatData;
import com.elmakers.mine.bukkit.utility.platform.v1_14.entity.EntityFoxData;
import com.elmakers.mine.bukkit.utility.platform.v1_14.entity.EntityPhantomData;

public class EntityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_13.EntityUtils  {
    public EntityUtils(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        switch (entity.getType()) {
            case FOX:
                return new EntityFoxData(entity);
            case CAT:
                return new EntityCatData(entity);
            case PHANTOM:
                return new EntityPhantomData(entity);
            default:
                return super.getExtraData(controller, entity);
        }
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case CAT:
                return new EntityCatData(parameters, controller);
            case FOX:
                return new EntityFoxData(parameters, controller);
            case PHANTOM:
                return new EntityPhantomData(parameters);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
