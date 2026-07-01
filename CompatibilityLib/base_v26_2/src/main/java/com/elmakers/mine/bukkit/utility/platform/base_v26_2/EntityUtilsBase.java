package com.elmakers.mine.bukkit.utility.platform.base_v26_2;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base_v26_2.entity.SulfurCubeData;

public class EntityUtilsBase extends com.elmakers.mine.bukkit.utility.platform.base_v26_1.EntityUtilsBase {
    protected EntityUtilsBase(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        switch (entity.getType()) {
            case SULFUR_CUBE:
                return new SulfurCubeData(entity);
            default:
                return super.getExtraData(controller, entity);
        }
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case SULFUR_CUBE:
                return new SulfurCubeData(parameters);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
