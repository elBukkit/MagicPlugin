package com.elmakers.mine.bukkit.utility.platform.base;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAbstractPiglinData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAxolotlData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityCatData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityEnderSignalData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityEndermiteData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityFallingBlockData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityFoxData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityGoatData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityHorseData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityLlamaData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityMooshroomData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityMuleData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityParrotData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityPhantomData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityShulkerData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityVillagerData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityZombieVillagerData;

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
            default:
                return super.getExtraData(controller, type, parameters);
        }
    }
}
