package com.elmakers.mine.bukkit.utility.platform.v1_11;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.v1_11.entity.EntityHorseData;
import com.elmakers.mine.bukkit.utility.platform.v1_11.entity.EntityLlamaData;
import com.elmakers.mine.bukkit.utility.platform.v1_11.entity.EntityMuleData;
import com.elmakers.mine.bukkit.utility.platform.v1_11.entity.EntityZombieVillagerData;

public class EntityUtils extends com.elmakers.mine.bukkit.utility.platform.legacy.EntityUtils  {
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
            case ZOMBIE_VILLAGER:
                return new EntityZombieVillagerData(entity);
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
            case ZOMBIE_VILLAGER:
                return new EntityZombieVillagerData(parameters, controller);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
