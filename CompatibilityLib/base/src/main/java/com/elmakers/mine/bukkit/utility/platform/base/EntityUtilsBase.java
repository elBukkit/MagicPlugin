package com.elmakers.mine.bukkit.utility.platform.base;

import org.bukkit.Art;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.EntityUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityAreaEffectCloudData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityArmorStandData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityCreeperData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityDroppedItemData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityEnderDragonData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityFallingBlockData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityHorseData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityItemFrameData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityPaintingData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityRabbitData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntitySlimeData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityVillagerData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityWolfData;
import com.elmakers.mine.bukkit.utility.platform.base.entity.EntityZombieData;

public abstract class EntityUtilsBase implements EntityUtils {
    protected final Platform platform;

    protected EntityUtilsBase(final Platform platform) {
        this.platform = platform;
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
        } else if (entity instanceof Painting) {
            extraData = new EntityPaintingData(entity);
        } else if (entity instanceof ItemFrame) {
            extraData = new EntityItemFrameData(entity);
        } else if (entity instanceof Item) {
            extraData = new EntityDroppedItemData(entity);
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
                return new EntityFallingBlockData(parameters, controller);
            case ENDER_DRAGON:
                return new EntityEnderDragonData(parameters, controller);
            case CREEPER:
                return new EntityCreeperData(parameters);
            case WOLF:
                return new EntityWolfData(parameters, controller);
            case PAINTING:
                return new EntityPaintingData(parameters, controller);
            case ITEM_FRAME:
                return new EntityItemFrameData(parameters, controller);
            case DROPPED_ITEM:
                return new EntityDroppedItemData(parameters, controller);
            default:
                return null;
        }
    }

    @Override
    public EntityExtraData getPaintingData(Art art, BlockFace direction) {
        return new EntityPaintingData(art, direction);
    }

    @Override
    public EntityExtraData getItemFrameData(ItemStack item, BlockFace direction, Rotation rotation) {
        return new EntityItemFrameData(item, direction, rotation);
    }
}
