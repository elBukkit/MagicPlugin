package com.elmakers.mine.bukkit.utility.platform.base_v1_20_5;

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
import org.bukkit.entity.Frog;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.platform.EntityUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.VersionedEntityType;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityAbstractPiglinData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityAreaEffectCloudData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityArmorStandData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityAxolotlData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityCatData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityCreeperData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityDroppedItemData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityEnderDragonData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityEnderSignalData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityEndermiteData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityFallingBlockData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityFoxData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityFrogData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityGoatData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityHorseData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityItemFrameData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityLlamaData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityMooshroomData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityMuleData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityNMSData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityPaintingData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityParrotData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityPhantomData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityRabbitData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntitySheepData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityShulkerData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntitySlimeData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityVillagerData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityWolfData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityZombieData;
import com.elmakers.mine.bukkit.utility.platform.base_v1_20_5.entity.EntityZombieVillagerData;

public class EntityUtilsBase implements EntityUtils {
    protected final Platform platform;

    protected EntityUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    private EntityExtraData getExtraDataByClass(MageController controller, Entity entity) {
        // This was the original implementation, it could probably be merged
        // with the new implementation by EntityType
        EntityExtraData extraData = null;
        if (entity instanceof Horse) {
            extraData = new EntityHorseData(entity, controller);
        } else if (entity instanceof Villager) {
            extraData = new EntityVillagerData(entity);
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
            extraData = new EntityFallingBlockData(entity, controller);
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
        } else if (entity instanceof Sheep) {
            extraData = new EntitySheepData(entity);
        } else if (entity instanceof Frog) {
            extraData = new EntityFrogData(entity);
        }
        return extraData;
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
                return getExtraDataByClass(controller, entity);
        }
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
            case ZOMBIE_VILLAGER:
                return new EntityZombieData(parameters);
            case ARMOR_STAND:
                return new EntityArmorStandData(parameters);
            case SLIME:
            case MAGMA_CUBE:
                return new EntitySlimeData(parameters);
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
            case ITEM:
                return new EntityDroppedItemData(parameters, controller);
            case SHEEP:
                return new EntitySheepData(parameters, controller);
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
            case MULE:
                return new EntityMuleData(parameters, controller);
            case LLAMA:
                return new EntityLlamaData(parameters, controller);
            case PARROT:
                return new EntityParrotData(parameters, controller);
            case SHULKER:
                return new EntityShulkerData(parameters);
            case GOAT:
                return new EntityGoatData(parameters, controller);
            case AXOLOTL:
                return new EntityAxolotlData(parameters, controller);
            case EYE_OF_ENDER:
                return new EntityEnderSignalData(parameters, controller);
            case FROG:
                return new EntityFrogData(parameters, controller);
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

    @Override
    public EntityType getEntityType(VersionedEntityType entityType) {
        switch (entityType) {
            case TNT: return EntityType.TNT;
            case ITEM: return EntityType.ITEM;
            default: return EntityType.UNKNOWN;
        }
    }

    @Override
    public EntityExtraData getNMSData(MageController controller, Object tag) {
        return new EntityNMSData(platform, tag);
    }
}
