package com.elmakers.mine.bukkit.utility.platform.legacy;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.EntityUtilsBase;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAreaEffectCloudData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityArmorStandData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityCreeperData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityEnderDragonData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityFallingBlockData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityHorseData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityRabbitData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntitySlimeData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityVillagerData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityWolfData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityZombieData;

public class EntityUtils extends EntityUtilsBase  {
    public EntityUtils(final Platform platform) {
        super(platform);
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, Entity entity) {
        EntityExtraData extraData = null;
        if (entity instanceof Horse) {
            extraData = new EntityHorseData((Horse)entity, controller);
        } else if (entity instanceof Villager) {
            extraData = new EntityVillagerData((Villager)entity);
        } else if (entity instanceof Wolf) {
            extraData = new EntityWolfData(entity);
        } else if (entity instanceof Rabbit) {
            extraData = new EntityRabbitData(entity);
        } else if (entity instanceof ArmorStand) {
            extraData = new EntityArmorStandData((ArmorStand)entity);
        } else if (entity instanceof Zombie) {
            extraData = new EntityZombieData((Zombie)entity);
        } else if (entity instanceof AreaEffectCloud) {
            extraData = new EntityAreaEffectCloudData((AreaEffectCloud)entity);
        } else if (entity instanceof Slime) {
            extraData = new EntitySlimeData((Slime)entity);
        } else if (entity instanceof FallingBlock) {
            extraData = new EntityFallingBlockData((FallingBlock)entity, controller);
        } else if (entity instanceof EnderDragon) {
            extraData = new EntityEnderDragonData(entity);
        } else if (entity instanceof Creeper) {
            extraData = new EntityCreeperData(entity);
        }
        return extraData;
    }

    @Override
    public EntityExtraData getExtraData(MageController controller, EntityType type, ConfigurationSection parameters) {
        switch (type) {
            case HORSE:
                return new EntityHorseData(parameters, controller);
            case VILLAGER:
                return new EntityVillagerData(parameters, controller);
            case AREA_EFFECT_CLOUD:
                return new EntityAreaEffectCloudData(parameters, controller);
            case RABBIT:
                return new EntityRabbitData(parameters, controller);
            case ZOMBIE:
            case PIG_ZOMBIE:
                return new EntityZombieData(parameters);
            case ARMOR_STAND:
                return new EntityArmorStandData(parameters);
            case SLIME:
            case MAGMA_CUBE:
                new EntitySlimeData(parameters);
            case FALLING_BLOCK:
                return new EntityFallingBlockData(parameters);
            case ENDER_DRAGON:
                return new EntityEnderDragonData(parameters, controller);
            case CREEPER:
                return new EntityCreeperData(parameters);
            case WOLF:
                return new EntityWolfData(parameters, controller);
            default:
                return null;
        }
    }
}
