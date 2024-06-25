package com.elmakers.mine.bukkit.utility.platform.v1_20_2;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.EntityUtilsBase;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityAbstractPiglinData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityAxolotlData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityCatData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityEnderSignalData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityEndermiteData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityFallingBlockData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityFoxData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityGoatData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityHorseData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityLlamaData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityMooshroomData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityMuleData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityParrotData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityPhantomData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityShulkerData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityVillagerData;
import com.elmakers.mine.bukkit.utility.platform.v1_20_2.entity.EntityZombieVillagerData;

public class EntityUtils extends EntityUtilsBase {
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
            case PARROT:
                return new EntityParrotData(entity);
            case SHULKER:
                return new EntityShulkerData(entity);
            case FALLING_BLOCK:
                // Falling blocks overridden here to use BlockData
                return new EntityFallingBlockData(entity, controller);
            case CAT:
                return new EntityCatData(entity);
            case FOX:
                return new EntityFoxData(entity);
            case PHANTOM:
                return new EntityPhantomData(entity);
            case ENDERMITE:
                return new EntityEndermiteData(entity);
            case VILLAGER:
                return new EntityVillagerData(entity);
            case PIGLIN:
            case PIGLIN_BRUTE:
                return new EntityAbstractPiglinData(entity);
            case MUSHROOM_COW:
                return new EntityMooshroomData(entity);
            case GOAT:
                return new EntityGoatData(entity);
            case AXOLOTL:
                return new EntityAxolotlData(entity);
            case ENDER_SIGNAL:
                return new EntityEnderSignalData(entity);
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
            case PARROT:
                return new EntityParrotData(parameters, controller);
            case SHULKER:
                return new EntityShulkerData(parameters);
            case FALLING_BLOCK:
                return new EntityFallingBlockData(parameters, controller);
            case CAT:
                return new EntityCatData(parameters, controller);
            case FOX:
                return new EntityFoxData(parameters, controller);
            case PHANTOM:
                return new EntityPhantomData(parameters);
            case ENDERMITE:
                return new EntityEndermiteData(parameters);
            case VILLAGER:
                return new EntityVillagerData(parameters, controller);
            case PIGLIN:
            case PIGLIN_BRUTE:
                return new EntityAbstractPiglinData(parameters, controller);
            case MUSHROOM_COW:
                return new EntityMooshroomData(parameters, controller);
            case GOAT:
                return new EntityGoatData(parameters, controller);
            case AXOLOTL:
                return new EntityAxolotlData(parameters, controller);
            case ENDER_SIGNAL:
                return new EntityEnderSignalData(parameters, controller);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
