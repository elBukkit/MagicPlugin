package com.elmakers.mine.bukkit.utility.platform.v1_16;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.v1_16.entity.EntityAbstractPiglinData;
import com.elmakers.mine.bukkit.utility.platform.v1_16.entity.EntityFoxData;
import com.elmakers.mine.bukkit.utility.platform.v1_16.entity.EntityMooshroomData;

public class EntityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_14.EntityUtils  {
    public EntityUtils(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        switch (entity.getType()) {
            case FOX:
                return new EntityFoxData(entity);
            case PIGLIN:
            case PIGLIN_BRUTE:
                return new EntityAbstractPiglinData(entity);
            case MUSHROOM_COW:
                return new EntityMooshroomData(entity);
            default:
                return super.getExtraData(controller, entity);
        }
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case FOX:
                return new EntityFoxData(parameters, controller);
            case PIGLIN:
            case PIGLIN_BRUTE:
                return new EntityAbstractPiglinData(parameters, controller);
            case MUSHROOM_COW:
                return new EntityMooshroomData(parameters, controller);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
