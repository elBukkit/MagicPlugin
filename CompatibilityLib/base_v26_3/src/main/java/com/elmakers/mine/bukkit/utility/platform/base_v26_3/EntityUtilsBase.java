package com.elmakers.mine.bukkit.utility.platform.base_v26_3;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base_v26_3.entity.CushionData;

public class EntityUtilsBase extends com.elmakers.mine.bukkit.utility.platform.base_v26_2.EntityUtilsBase {
    protected EntityUtilsBase(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        switch (entity.getType()) {
            case CUSHION:
                return new CushionData(entity);
            default:
                return super.getExtraData(controller, entity);
        }
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case CUSHION:
                return new CushionData(parameters);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
