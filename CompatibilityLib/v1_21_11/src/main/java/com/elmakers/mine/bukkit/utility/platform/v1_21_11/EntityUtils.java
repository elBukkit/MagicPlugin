package com.elmakers.mine.bukkit.utility.platform.v1_21_11;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.EntityUtilsBase;
import com.elmakers.mine.bukkit.utility.platform.v1_21_11.entity.EntityBoggedData;
import com.elmakers.mine.bukkit.utility.platform.v1_21_11.entity.EntityChickenData;
import com.elmakers.mine.bukkit.utility.platform.v1_21_11.entity.EntityCowData;
import com.elmakers.mine.bukkit.utility.platform.v1_21_11.entity.EntityPigData;
import com.elmakers.mine.bukkit.utility.platform.v1_21_11.entity.EntityZombieNautilusData;

public class EntityUtils extends EntityUtilsBase {

    protected EntityUtils(Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        switch (entity.getType()) {
            case BOGGED:
                return new EntityBoggedData(entity);
            case CHICKEN:
                return new EntityChickenData(entity);
            case COW:
                return new EntityCowData(entity);
            case PIG:
                return new EntityPigData(entity);
            case ZOMBIE_NAUTILUS:
                return new EntityZombieNautilusData(entity);
            default:
                return super.getExtraData(controller, entity);
        }
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case BOGGED:
                return new EntityBoggedData(parameters, controller);
            case CHICKEN:
                return new EntityChickenData(parameters, controller);
            case COW:
                return new EntityCowData(parameters, controller);
            case PIG:
                return new EntityPigData(parameters, controller);
            case ZOMBIE_NAUTILUS:
                return new EntityZombieNautilusData(parameters, controller);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
