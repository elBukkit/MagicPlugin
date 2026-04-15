package com.elmakers.mine.bukkit.utility.platform.v1_12;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.v1_12.entity.EntityHorseData;
import com.elmakers.mine.bukkit.utility.platform.v1_12.entity.EntityLlamaData;
import com.elmakers.mine.bukkit.utility.platform.v1_12.entity.EntityMuleData;
import com.elmakers.mine.bukkit.utility.platform.v1_12.entity.EntityParrotData;
import com.elmakers.mine.bukkit.utility.platform.v1_12.entity.EntityShulkerData;

public class EntityUtils extends com.elmakers.mine.bukkit.utility.platform.v1_11.EntityUtils  {
    public EntityUtils(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        switch (entity.getType()) {
            case HORSE:
                return new EntityHorseData(entity, controller);
            case LLAMA:
                return new EntityLlamaData(entity, controller);
            case MULE:
                return new EntityMuleData(entity, controller);
            case PARROT:
                return new EntityParrotData(entity);
            case SHULKER:
                return new EntityShulkerData(entity);
            default:
                return super.getExtraData(controller, entity);
        }
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case HORSE:
                return new EntityHorseData(parameters, controller);
            case MULE:
                return new EntityMuleData(parameters, controller);
            case LLAMA:
                return new EntityLlamaData(parameters, controller);
            case PARROT:
                return new EntityParrotData(parameters, controller);
            case SHULKER:
                return new EntityShulkerData(parameters);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
