package com.elmakers.mine.bukkit.utility.platform.modern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityAbstractPiglinData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityAxolotlData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityCatData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityEnderSignalData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityEndermiteData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityFallingBlockData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityFoxData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityGoatData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityHorseData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityLlamaData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityMooshroomData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityMuleData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityParrotData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityPhantomData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityShulkerData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityVillagerData;
import com.elmakers.mine.bukkit.utility.platform.modern.entity.EntityZombieVillagerData;

public class ModernEntityUtils extends com.elmakers.mine.bukkit.utility.platform.base.EntityUtilsBase  {
    public ModernEntityUtils(final Platform platform) {
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
            case MOOSHROOM:
                return new EntityMooshroomData(entity);
            case CAT:
                return new EntityCatData(entity);
            case PHANTOM:
                return new EntityPhantomData(entity);
            case ENDERMITE:
                return new EntityEndermiteData(entity);
            case VILLAGER:
                return new EntityVillagerData(entity);
            case FALLING_BLOCK:
                return new EntityFallingBlockData(entity, controller);
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
            case ZOMBIE_VILLAGER:
                return new EntityZombieVillagerData(entity);
            case GOAT:
                return new EntityGoatData(entity);
            case AXOLOTL:
                return new EntityAxolotlData(entity);
            case EYE_OF_ENDER:
                return new EntityEnderSignalData(entity);
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
            case MOOSHROOM:
                return new EntityMooshroomData(parameters, controller);
            case CAT:
                return new EntityCatData(parameters, controller);
            case PHANTOM:
                return new EntityPhantomData(parameters);
            case ENDERMITE:
                return new EntityEndermiteData(parameters);
            case VILLAGER:
                return new EntityVillagerData(parameters, controller);
            case FALLING_BLOCK:
                return new EntityFallingBlockData(parameters, controller);
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
            case ZOMBIE_VILLAGER:
                return new EntityZombieVillagerData(parameters, controller);
            case GOAT:
                return new EntityGoatData(parameters, controller);
            case AXOLOTL:
                return new EntityAxolotlData(parameters, controller);
            case EYE_OF_ENDER:
                return new EntityEnderSignalData(parameters, controller);
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
