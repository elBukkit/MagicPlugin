package com.elmakers.mine.bukkit.utility.platform.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.google.common.base.CaseFormat;

/**
 * Contains some raw methods for doing some simple NMS utilities.
 *
 * <p>This is not meant to be a replacement for full-on NMS or Protocol libs,
 * but it is enough for Magic to use internally without requiring any
 * external dependencies.
 *
 * <p>Use any of this at your own risk!
 */
@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public class NMSUtils {
    protected static String versionPrefix;
    protected static boolean failed = false;
    protected static boolean hasStatistics = false;
    protected static boolean hasEntityTransformEvent = false;
    protected static boolean hasTimeSkipEvent = false;

    protected static Class<?> class_NBTTagCompound;
    protected static Class<?> class_CraftBlock;
    protected static Class<?> class_World;
    protected static Class<?> class_Packet;
    protected static Class<?> class_IBlockData;
    protected static Class<?> class_TileEntityRecordPlayer;
    protected static Class<Enum> class_EnumDirection;
    protected static Class<?> class_PacketPlayOutEntityDestroy;
    protected static Class<?> class_IProjectile;
    protected static Class<?> class_EntityProjectile;
    protected static Class<?> class_EntityFireball;
    protected static Class<?> class_EntityArrow;
    protected static Class<?> class_PacketPlayOutChat;
    protected static Enum<?> enum_ChatMessageType_GAME_INFO;
    protected static Class<?> class_entityTypes;
    protected static Class<?> class_Lootable;
    protected static Enum<?> enum_EnumHand_MAIN_HAND;

    protected static Method class_NBTTagList_getDoubleMethod;
    protected static Method class_Entity_setYawPitchMethod;
    protected static Method class_Entity_getBukkitEntityMethod;
    protected static Method class_EntityLiving_damageEntityMethod;
    protected static Method class_DamageSource_getMagicSourceMethod;
    protected static Method class_EntityDamageSource_setThornsMethod;
    protected static Method class_NBTTagCompound_setIntMethod;
    protected static Method class_NBTTagCompound_removeMethod;
    protected static Method class_NBTTagCompound_getMethod;
    protected static Method class_NBTTagCompound_getIntArrayMethod;
    protected static Method class_NBTTagCompound_getListMethod;
    protected static Method class_Entity_saveMethod;
    protected static Method class_Entity_getTypeMethod;
    protected static Method class_TileEntity_loadMethod;
    protected static Method class_TileEntity_saveMethod;
    protected static Method class_TileEntity_updateMethod;
    protected static Method class_World_addEntityMethod;
    protected static Method class_World_setTypeAndDataMethod;
    protected static Method class_World_getTypeMethod;
    protected static Method class_CraftItemStack_copyMethod;
    protected static Method class_CraftWorld_getTileEntityAtMethod;
    protected static Method class_CraftWorld_spawnMethod;
    protected static boolean class_CraftWorld_spawnMethod_isLegacy;
    protected static Method class_Entity_setLocationMethod;
    protected static Method class_Entity_getIdMethod;
    protected static Method class_Entity_getDataWatcherMethod;
    protected static Method class_Entity_setInvisible;
    protected static Method class_Entity_isInvisible;
    protected static Method class_CraftPlayer_getHandleMethod;
    protected static Method class_CraftChunk_getHandleMethod;
    protected static Method class_CraftEntity_getHandleMethod;
    protected static Method class_CraftLivingEntity_getHandleMethod;
    protected static Method class_CraftWorld_getHandleMethod;
    protected static Method class_CraftServer_getServerMethod;
    protected static Method class_ProjectileHitEvent_getHitBlockMethod;
    protected static Method class_ItemDye_bonemealMethod;
    protected static Method class_World_getTileEntityMethod;
    protected static Method class_Lootable_setLootTableMethod;
    protected static Method class_CraftArt_NotchToBukkitMethod;
    protected static Method class_BlockPosition_getXMethod;
    protected static Method class_BlockPosition_getYMethod;
    protected static Method class_BlockPosition_getZMethod;
    protected static Method class_CraftBlock_getNMSBlockMethod;
    protected static Method class_Block_getPlacedStateMethod;
    protected static Method class_MovingObjectPositionBlock_createMethod;
    protected static Method class_CraftBlock_setTypeAndDataMethod;
    protected static Method class_nms_Block_getBlockDataMethod;

    protected static Constructor class_EntityFireworkConstructor;
    protected static Constructor class_EntityPaintingConstructor;
    protected static Constructor class_EntityItemFrameConstructor;
    protected static Constructor class_BlockPosition_Constructor;
    protected static Constructor class_PacketSpawnEntityConstructor;
    protected static Constructor class_PacketPlayOutEntityMetadata_Constructor;
    protected static Constructor class_PacketPlayOutEntityStatus_Constructor;
    protected static Constructor class_PacketPlayOutBlockBreakAnimation_Constructor;
    protected static Constructor class_NBTTagCompound_constructor;
    protected static Constructor class_PacketPlayOutChat_constructor;
    protected static Constructor class_ChatComponentText_constructor;
    protected static Constructor class_Vec3D_constructor;
    protected static Constructor class_BlockActionContext_constructor;

    protected static Field class_Entity_motField;
    protected static Field class_Firework_ticksFlownField;
    protected static Field class_Firework_expectedLifespanField;
    protected static Field class_EntityFallingBlock_hurtEntitiesField;
    protected static Field class_EntityFallingBlock_fallHurtMaxField;
    protected static Field class_EntityFallingBlock_fallHurtAmountField;
    protected static Field class_EntityArmorStand_disabledSlotsField;
    protected static Field class_EntityPlayer_playerConnectionField;
    protected static Field class_PlayerConnection_floatCountField;
    protected static Field class_EntityArrow_lifeField = null;
    protected static Field class_EntityArrow_damageField;
    protected static Field class_Entity_jumpingField;
    protected static Field class_Entity_moveStrafingField;
    protected static Field class_Entity_moveForwardField;
    protected static Field class_TileEntityRecordPlayer_record;
    protected static Field class_EntityPainting_art;
    protected static Field class_EntityHanging_blockPosition;
    protected static Field class_Entity_persistentInvisibilityField;

    protected static Object object_magicSource;
    protected static Map<String, Object> damageSources;
    protected static Map<String, Object> entityTypes;

    protected static boolean chatPacketHasUUID = false;

    public static boolean initialize(Platform platform) {
        Logger logger = platform.getLogger();
        versionPrefix = CompatibilityConstants.getVersionPrefix();
        try {
            Class<?> class_Block = fixBukkitClass("net.minecraft.server.Block");
            Class<?> class_Entity = fixBukkitClass("net.minecraft.server.Entity");
            Class<?> class_EntityLiving = fixBukkitClass("net.minecraft.server.EntityLiving");
            Class<?> class_EntityHuman = fixBukkitClass("net.minecraft.server.EntityHuman");
            Class<?> class_ItemStack = fixBukkitClass("net.minecraft.server.ItemStack");
            Class<?> class_DataWatcher = fixBukkitClass("net.minecraft.server.DataWatcher");
            class_NBTTagCompound = fixBukkitClass("net.minecraft.server.NBTTagCompound");
            Class<?> class_NBTTagList = fixBukkitClass("net.minecraft.server.NBTTagList");
            Class<?> class_CraftWorld = fixBukkitClass("org.bukkit.craftbukkit.CraftWorld");
            Class<?> class_CraftItemStack = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftItemStack");
            Class<?> class_CraftLivingEntity = fixBukkitClass("org.bukkit.craftbukkit.entity.CraftLivingEntity");
            class_Packet = fixBukkitClass("net.minecraft.server.Packet");
            class_World = fixBukkitClass("net.minecraft.server.World");
            Class<?> class_WorldServer = fixBukkitClass("net.minecraft.server.WorldServer");
            Class<?> class_EntityPainting = fixBukkitClass("net.minecraft.server.EntityPainting");
            Class<?> class_EntityItemFrame = fixBukkitClass("net.minecraft.server.EntityItemFrame");
            Class<?> class_AxisAlignedBB = fixBukkitClass("net.minecraft.server.AxisAlignedBB");
            Class<?> class_DamageSource = fixBukkitClass("net.minecraft.server.DamageSource");
            Class<?> class_EntityDamageSource = fixBukkitClass("net.minecraft.server.EntityDamageSource");
            Class<?> class_EntityFirework = fixBukkitClass("net.minecraft.server.EntityFireworks");
            Class<?> class_TileEntity = fixBukkitClass("net.minecraft.server.TileEntity");
            class_PacketPlayOutEntityDestroy = fixBukkitClass("net.minecraft.server.PacketPlayOutEntityDestroy");
            Class<?> class_PacketPlayOutSpawnEntity = fixBukkitClass("net.minecraft.server.PacketPlayOutSpawnEntity");
            Class<?> class_PacketPlayOutEntityMetadata = fixBukkitClass("net.minecraft.server.PacketPlayOutEntityMetadata");
            Class<?> class_PacketPlayOutEntityStatus = fixBukkitClass("net.minecraft.server.PacketPlayOutEntityStatus");
            Class<?> class_PacketPlayOutBlockBreakAnimation = fixBukkitClass("net.minecraft.server.PacketPlayOutBlockBreakAnimation");
            Class<?> class_EntityFallingBlock = fixBukkitClass("net.minecraft.server.EntityFallingBlock");
            Class<?> class_EntityArmorStand = fixBukkitClass("net.minecraft.server.EntityArmorStand");
            Class<?> class_EntityPlayer = fixBukkitClass("net.minecraft.server.EntityPlayer");
            Class<?> class_PlayerConnection = fixBukkitClass("net.minecraft.server.PlayerConnection");
            Class<?> class_CraftPlayer = fixBukkitClass("org.bukkit.craftbukkit.entity.CraftPlayer");
            Class<?> class_CraftChunk = fixBukkitClass("org.bukkit.craftbukkit.CraftChunk");
            Class<?> class_CraftEntity = fixBukkitClass("org.bukkit.craftbukkit.entity.CraftEntity");
            Class<?> class_TileEntitySign = fixBukkitClass("net.minecraft.server.TileEntitySign");
            try {
                // 1.13
                class_TileEntityRecordPlayer = fixBukkitClass("net.minecraft.server.TileEntityJukeBox");
            } catch (ClassNotFoundException e) {
                // <= 1.12
                class_TileEntityRecordPlayer = fixBukkitClass("net.minecraft.server.BlockJukeBox$TileEntityRecordPlayer");
            }
            Class<?> class_CraftServer = fixBukkitClass("org.bukkit.craftbukkit.CraftServer");
            Class<?> class_MinecraftServer = fixBukkitClass("net.minecraft.server.MinecraftServer");
            Class<?> class_BlockPosition = fixBukkitClass("net.minecraft.server.BlockPosition");

            class_EntityProjectile = NMSUtils.getBukkitClass("net.minecraft.server.EntityProjectile");
            class_EntityFireball = NMSUtils.getBukkitClass("net.minecraft.server.EntityFireball");
            class_EntityArrow = NMSUtils.getBukkitClass("net.minecraft.server.EntityArrow");

            class_Entity_getBukkitEntityMethod = class_Entity.getMethod("getBukkitEntity");
            class_Entity_setYawPitchMethod = class_Entity.getDeclaredMethod("setYawPitch", Float.TYPE, Float.TYPE);
            class_Entity_setYawPitchMethod.setAccessible(true);
            class_NBTTagCompound_setIntMethod = class_NBTTagCompound.getMethod("setInt", String.class, Integer.TYPE);
            class_NBTTagCompound_removeMethod = class_NBTTagCompound.getMethod("remove", String.class);
            class_NBTTagCompound_getListMethod = class_NBTTagCompound.getMethod("getList", String.class, Integer.TYPE);
            class_CraftItemStack_copyMethod = class_CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
            class_World_addEntityMethod = class_World.getMethod("addEntity", class_Entity, CreatureSpawnEvent.SpawnReason.class);
            class_Entity_setLocationMethod = class_Entity.getMethod("setLocation", Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Float.TYPE);
            class_Entity_getIdMethod = class_Entity.getMethod("getId");
            class_Entity_getDataWatcherMethod = class_Entity.getMethod("getDataWatcher");
            class_CraftPlayer_getHandleMethod = class_CraftPlayer.getMethod("getHandle");
            class_CraftChunk_getHandleMethod = class_CraftChunk.getMethod("getHandle");
            class_CraftEntity_getHandleMethod = class_CraftEntity.getMethod("getHandle");
            class_CraftLivingEntity_getHandleMethod = class_CraftLivingEntity.getMethod("getHandle");
            class_CraftWorld_getHandleMethod = class_CraftWorld.getMethod("getHandle");
            class_CraftServer_getServerMethod = class_CraftServer.getMethod("getServer");

            class_EntityFireworkConstructor = class_EntityFirework.getConstructor(class_World, Double.TYPE, Double.TYPE, Double.TYPE, class_ItemStack);
            class_PacketSpawnEntityConstructor = class_PacketPlayOutSpawnEntity.getConstructor(class_Entity, Integer.TYPE);
            class_PacketPlayOutEntityMetadata_Constructor = class_PacketPlayOutEntityMetadata.getConstructor(Integer.TYPE, class_DataWatcher, Boolean.TYPE);
            class_PacketPlayOutEntityStatus_Constructor = class_PacketPlayOutEntityStatus.getConstructor(class_Entity, Byte.TYPE);
            class_PacketPlayOutBlockBreakAnimation_Constructor = class_PacketPlayOutBlockBreakAnimation.getConstructor(Integer.TYPE, class_BlockPosition, Integer.TYPE);

            class_EntityPlayer_playerConnectionField = class_EntityPlayer.getDeclaredField("playerConnection");

            class_Firework_ticksFlownField = class_EntityFirework.getDeclaredField("ticksFlown");
            class_Firework_ticksFlownField.setAccessible(true);
            class_Firework_expectedLifespanField = class_EntityFirework.getDeclaredField("expectedLifespan");
            class_Firework_expectedLifespanField.setAccessible(true);

            class_NBTTagCompound_constructor = class_NBTTagCompound.getConstructor();
            class_NBTTagCompound_getMethod = class_NBTTagCompound.getMethod("get", String.class);

            class_EntityFallingBlock_hurtEntitiesField = class_EntityFallingBlock.getDeclaredField("hurtEntities");
            class_EntityFallingBlock_hurtEntitiesField.setAccessible(true);
            class_EntityFallingBlock_fallHurtAmountField = class_EntityFallingBlock.getDeclaredField("fallHurtAmount");
            class_EntityFallingBlock_fallHurtAmountField.setAccessible(true);
            class_EntityFallingBlock_fallHurtMaxField = class_EntityFallingBlock.getDeclaredField("fallHurtMax");
            class_EntityFallingBlock_fallHurtMaxField.setAccessible(true);

            class_EnumDirection = (Class<Enum>) fixBukkitClass("net.minecraft.server.EnumDirection");
            class_BlockPosition_Constructor = class_BlockPosition.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE);
            class_EntityPaintingConstructor = class_EntityPainting.getConstructor(class_World, class_BlockPosition, class_EnumDirection);
            class_EntityItemFrameConstructor = class_EntityItemFrame.getConstructor(class_World, class_BlockPosition, class_EnumDirection);

            try {
                Class.forName("org.bukkit.event.player.PlayerStatisticIncrementEvent");
                hasStatistics = true;
            } catch (Throwable ex) {
                hasStatistics = false;
                logger.warning("Statistics not available, jump trigger will not work");
            }

            try {
                class_IProjectile = NMSUtils.getBukkitClass("net.minecraft.server.IProjectile");
            } catch (Throwable ignore) {
            }

            try {
                Class.forName("org.bukkit.event.entity.EntityTransformEvent");
                hasEntityTransformEvent = true;
            } catch (Throwable ex) {
                hasEntityTransformEvent = false;
                logger.warning("EntityTransformEvent not found, can't prevent mobs naturally transforming");
            }

            try {
                Class.forName("org.bukkit.event.world.TimeSkipEvent");
                hasTimeSkipEvent = true;
            } catch (Throwable ex) {
                hasTimeSkipEvent = false;
                logger.warning("TimeSkipEvent not found, can't synchronize time between worlds");
            }

            try {
                class_Entity_setInvisible = class_Entity.getDeclaredMethod("setInvisible", Boolean.TYPE);
                class_Entity_isInvisible = class_Entity.getDeclaredMethod("isInvisible");
            } catch (Throwable ignore) {
                logger.warning("Entity.setInvisible method not found, can't set every entity type invisible");
                class_Entity_setInvisible = null;
                class_Entity_isInvisible = null;
            }

            try {
                class_Entity_persistentInvisibilityField = class_Entity.getDeclaredField("persistentInvisibility");
            } catch (Throwable ignore) {
                logger.warning("Entity.persistentInvisibility field not found, invisibility may not reliably restore");
                class_Entity_persistentInvisibilityField = null;
            }

            try {
                Class<?> class_CraftArt = fixBukkitClass("org.bukkit.craftbukkit.CraftArt");
                class_CraftArt_NotchToBukkitMethod = class_CraftArt.getMethod("BukkitToNotch", Art.class);
                class_EntityPainting_art = class_EntityPainting.getDeclaredField("art");
                class_EntityPainting_art.setAccessible(true);
                Class<?> class_EntityHanging = fixBukkitClass("net.minecraft.server.EntityHanging");
                class_EntityHanging_blockPosition = class_EntityHanging.getField("blockPosition");
                class_EntityHanging_blockPosition.setAccessible(true);
                class_BlockPosition_getXMethod = class_BlockPosition.getMethod("getX");
                class_BlockPosition_getYMethod = class_BlockPosition.getMethod("getY");
                class_BlockPosition_getZMethod = class_BlockPosition.getMethod("getZ");
            } catch (Throwable ex) {
                class_EntityPainting_art = null;
                class_CraftArt_NotchToBukkitMethod = null;
                class_EntityHanging_blockPosition = null;
                logger.warning("Could not bind to painting art fields, restoring paintings may not work");
            }

            // 1.13 Support
            try {
                Class<?> class_Vec3D = fixBukkitClass("net.minecraft.server.Vec3D");
                class_Vec3D_constructor = class_Vec3D.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE);
            } catch (Throwable ex) {
                class_Vec3D_constructor = null;
            }
            try {
                class_Lootable = Class.forName("org.bukkit.loot.Lootable");
                Class<?> class_LootTable = Class.forName("org.bukkit.loot.LootTable");
                class_Lootable_setLootTableMethod = class_Lootable.getMethod("setLootTable", class_LootTable);
            } catch (Exception ex) {
                class_Lootable = null;
                class_Lootable_setLootTableMethod = null;
            }
            try {
                entityTypes = new HashMap<>();
                class_entityTypes = fixBukkitClass("net.minecraft.server.EntityTypes");
                for (Field field : class_entityTypes.getFields()) {
                    if (field.getType().equals(class_entityTypes)) {
                        Object entityType = field.get(null);
                        // This won't really work for all entity types, but it should work for most projectiles
                        // which is all this map is used for.
                        String name = field.getName();
                        name = "Entity" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name);
                        entityTypes.put(name, entityType);

                        // We may need to do some manual fixups here.
                        if (name.equals("EntityArrow")) {
                            entityTypes.put("EntityTippedArrow", entityType);
                        } else if (name.equals("EntityFireball")) {
                            entityTypes.put("EntityLargeFireball", entityType);
                        } else if (name.equals("EntityTrident")) {
                            entityTypes.put("EntityThrownTrident", entityType);
                        } else if (name.equals("EntityExperienceBottle")) {
                            entityTypes.put("EntityThrownExpBottle", entityType);
                        } else if (name.equals("EntityExperienceBottle")) {
                            entityTypes.put("EntityThrownExpBottle", entityType);
                        } else if (name.equals("EntityFishingBobber")) {
                            entityTypes.put("EntityFishingHook", entityType);
                        }
                    }
                }
            } catch (Throwable not14) {
                logger.warning("Could not bind to entity types, projectile launches will not work");
            }

            try {
                class_Entity_motField = class_Entity.getDeclaredField("mot");
                class_Entity_motField.setAccessible(true);
            } catch (Throwable not14) {
                logger.warning("Could not bind to motion setters, some things may be broken");
            }

            try {
                Class<?> class_ItemBoneMeal = fixBukkitClass("net.minecraft.server.ItemBoneMeal");
                class_ItemDye_bonemealMethod = class_ItemBoneMeal.getMethod("a", class_ItemStack, class_World, class_BlockPosition);
            } catch (Throwable not13) {
                try {
                    Class<?> class_ItemDye = fixBukkitClass("net.minecraft.server.ItemDye");
                    class_ItemDye_bonemealMethod = class_ItemDye.getMethod("a", class_ItemStack, class_World, class_BlockPosition);
                } catch (Throwable ex) {
                    class_ItemDye_bonemealMethod = null;
                    logger.info("Couldn't bind to ItemDye bonemeal method, Bonemeal action will not work");
                }
            }

            try {
                class_NBTTagCompound_getIntArrayMethod = class_NBTTagCompound.getMethod("getIntArray", String.class);
            } catch (Throwable ex) {
                class_NBTTagCompound_getIntArrayMethod = null;
                logger.info("Couldn't bind to NBT getIntArray method, pasting tile entities from schematics may not work");
            }

            try {
                class_NBTTagList_getDoubleMethod = class_NBTTagList.getMethod("h", Integer.TYPE);
                if (class_NBTTagList_getDoubleMethod.getReturnType() != Double.TYPE) {
                    throw new Exception("Wrong return type");
                }
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred while registering NBTTagList.getDouble, loading entities from schematics will not work", ex);
                class_NBTTagList_getDoubleMethod = null;
            }
            try {
                Class<?> class_IBlockData = fixBukkitClass("net.minecraft.server.IBlockData");
                class_World_getTypeMethod = class_World.getMethod("getType", class_BlockPosition);
                class_World_setTypeAndDataMethod = class_World.getMethod("setTypeAndData", class_BlockPosition, class_IBlockData, Integer.TYPE);
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred while registering World.setTypeAndData, Deferred physics updates will not work", ex);
                class_World_setTypeAndDataMethod = null;
            }

            try {
                try {
                    class_DamageSource_getMagicSourceMethod = class_DamageSource.getMethod("c", class_Entity, class_Entity);
                } catch (Throwable not13) {
                    class_DamageSource_getMagicSourceMethod = class_DamageSource.getMethod("b", class_Entity, class_Entity);
                }
                class_EntityLiving_damageEntityMethod = class_EntityLiving.getMethod("damageEntity", class_DamageSource, Float.TYPE);
                Field damageSource_MagicField = class_DamageSource.getField("MAGIC");
                object_magicSource = damageSource_MagicField.get(null);
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred, magic damage will not work, using normal damage instead", ex);
                class_EntityLiving_damageEntityMethod = null;
                class_DamageSource_getMagicSourceMethod = null;
                object_magicSource = null;
            }
            try {
                class_ProjectileHitEvent_getHitBlockMethod = ProjectileHitEvent.class.getMethod("getHitBlock");
            } catch (Throwable ex) {
                class_ProjectileHitEvent_getHitBlockMethod = null;
                logger.warning("Could not register ProjectileHitEvent.getHitBlock, arrow hit locations will be fuzzy");
            }

            try {
                class_Entity_jumpingField = class_EntityLiving.getDeclaredField("jumping");
                class_Entity_jumpingField.setAccessible(true);
                class_Entity_moveStrafingField = class_EntityLiving.getDeclaredField("aR");
                class_Entity_moveForwardField = class_EntityLiving.getDeclaredField("aT");

                if (class_Entity_moveStrafingField.getType() != Float.TYPE || class_Entity_moveForwardField.getType() != Float.TYPE) {
                    throw new Exception("Movement fields are of the wrong type");
                }
                if (!isPublic(class_Entity_moveStrafingField) || !isPublic(class_Entity_moveForwardField)) {
                    throw new Exception("Could not find accessible methods");
                }
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred while registering entity movement accessors", ex);
                class_Entity_jumpingField = null;
                class_Entity_moveStrafingField = null;
                class_Entity_moveForwardField = null;
            }

            try {
                // Common to 1.12 and below
                class_PacketPlayOutChat = fixBukkitClass("net.minecraft.server.PacketPlayOutChat");
                Class<?> class_ChatComponentText = fixBukkitClass("net.minecraft.server.ChatComponentText");
                Class<?> class_IChatBaseComponent = fixBukkitClass("net.minecraft.server.IChatBaseComponent");
                class_ChatComponentText_constructor = class_ChatComponentText.getConstructor(String.class);

                // Common to 1.16 and below
                Class<Enum> class_ChatMessageType = (Class<Enum>) fixBukkitClass("net.minecraft.server.ChatMessageType");
                enum_ChatMessageType_GAME_INFO = Enum.valueOf(class_ChatMessageType, "GAME_INFO");
                class_PacketPlayOutChat_constructor = class_PacketPlayOutChat.getConstructor(class_IChatBaseComponent, class_ChatMessageType, UUID.class);
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred while registering action bar methods, action bar messages will not work", ex);
                class_PacketPlayOutChat = null;
                class_PacketPlayOutChat_constructor = null;
            }

            try {
                Class<?> class_Consumer = fixBukkitClass("org.bukkit.util.Consumer");
                class_CraftWorld_spawnMethod = class_CraftWorld.getMethod("spawn", Location.class, Class.class, class_Consumer, CreatureSpawnEvent.SpawnReason.class);
                class_CraftWorld_spawnMethod_isLegacy = false;
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred while registering custom spawn method, spawn reasons will not work", ex);
                class_CraftWorld_spawnMethod = null;
            }

            try {
                try {
                    class_IBlockData = fixBukkitClass("net.minecraft.server.IBlockData");
                    class_TileEntity_loadMethod = class_TileEntity.getMethod("load", class_IBlockData, class_NBTTagCompound);
                } catch (Throwable not16) {
                    class_IBlockData = null;
                    try {
                        // 1.12.1
                        class_TileEntity_loadMethod = class_TileEntity.getMethod("load", class_NBTTagCompound);
                    } catch (Throwable ignore) {
                        class_TileEntity_loadMethod = class_TileEntity.getMethod("a", class_NBTTagCompound);
                    }
                }
                try {
                    class_CraftWorld_getTileEntityAtMethod = class_CraftWorld.getMethod("getTileEntityAt", Integer.TYPE, Integer.TYPE, Integer.TYPE);
                } catch (Throwable ignore) {
                    // This should actually work in 1.10 and up at least, and is the preferred method to use.
                    // For now we're going to keep old versions doing it the old way, though.

                    class_World_getTileEntityMethod = class_World.getMethod("getTileEntity", class_BlockPosition);
                }
                class_TileEntity_updateMethod = class_TileEntity.getMethod("update");
                class_TileEntity_saveMethod = class_TileEntity.getMethod("save", class_NBTTagCompound);
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred, handling of tile entities may not work well", ex);
                class_TileEntity_loadMethod = null;
                class_TileEntity_updateMethod = null;
                class_TileEntity_saveMethod = null;
            }

            try {
                damageSources = new HashMap<>();
                Field[] fields = class_DamageSource.getFields();
                for (Field field : fields) {
                    if (class_DamageSource.isAssignableFrom(field.getType())) {
                        damageSources.put(field.getName(), field.get(null));
                    }
                }
            } catch (Throwable ex) {
                damageSources = null;
                logger.log(Level.WARNING, "An error occurred, using specific damage types will not work, will use normal damage instead", ex);
            }

            try {
                class_EntityArmorStand_disabledSlotsField = class_EntityArmorStand.getDeclaredField("disabledSlots");
                if (class_EntityArmorStand_disabledSlotsField.getType() != Integer.TYPE) {
                    throw new Exception("Wrong return type");
                }
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred, armor stand slots cannot be locked", ex);
                class_EntityArmorStand_disabledSlotsField = null;
            }
            if (class_EntityArmorStand_disabledSlotsField != null) {
                class_EntityArmorStand_disabledSlotsField.setAccessible(true);
            }

            try {
                class_TileEntityRecordPlayer_record = class_TileEntityRecordPlayer.getDeclaredField("a");
                class_TileEntityRecordPlayer_record.setAccessible(true);
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "Failed to find 'record' field in jukebox tile entity", ex);
                class_TileEntityRecordPlayer_record = null;
            }

            try {
                class_PlayerConnection_floatCountField = class_PlayerConnection.getDeclaredField("C");
                if (class_PlayerConnection_floatCountField.getType() != Integer.TYPE) {
                    throw new Exception("Wrong return type");
                }
                class_PlayerConnection_floatCountField.setAccessible(true);
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred, player flight exemption will not work", ex);
                class_PlayerConnection_floatCountField = null;
            }

            try {
                try { // Purpur fork
                    Class<?> class_IProjectile = fixBukkitClass("net.minecraft.server.IProjectile");
                    class_EntityArrow_lifeField = class_IProjectile.getDeclaredField("despawnCounter");
                } catch (Throwable ignore) {
                    // 1.13
                    class_EntityArrow_lifeField = class_EntityArrow.getDeclaredField("despawnCounter");
                }
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "Could not find arrow lifetime field, setting arrow lifespan will not work");
                class_EntityArrow_lifeField = null;
            }
            if (class_EntityArrow_lifeField != null) {
                class_EntityArrow_lifeField.setAccessible(true);
            }

            try {
                class_EntityDamageSource_setThornsMethod = class_EntityDamageSource.getMethod("x");
                if (!class_EntityDamageSource_setThornsMethod.getReturnType().isAssignableFrom(class_EntityDamageSource)) {
                    throw new Exception("Wrong return type");
                }
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred, thorn damage override to hurt ender dragon will not work", ex);
                class_EntityDamageSource_setThornsMethod = null;
            }

            try {
                class_Entity_getTypeMethod = class_Entity.getDeclaredMethod("getSaveID");
                class_Entity_saveMethod = class_Entity.getMethod("save", class_NBTTagCompound);
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred, saving entities to spawn eggs will not work", ex);
                class_Entity_getTypeMethod = null;
                class_Entity_saveMethod = null;
            }
            if (class_Entity_getTypeMethod != null) {
                class_Entity_getTypeMethod.setAccessible(true);
            }

            try {
                class_EntityArrow_damageField = class_EntityArrow.getDeclaredField("damage");
                class_EntityArrow_damageField.setAccessible(true);
            } catch (Throwable ex) {
                logger.log(Level.WARNING, "An error occurred, setting arrow damage will not work", ex);
                class_EntityArrow_damageField = null;
            }

            // Auto block state creation
            try {
                class_CraftBlock = fixBukkitClass("org.bukkit.craftbukkit.block.CraftBlock");
                class_CraftBlock_getNMSBlockMethod = class_CraftBlock.getDeclaredMethod("getNMSBlock");
                class_CraftBlock_getNMSBlockMethod.setAccessible(true);
                Class<?> class_BlockActionContext = fixBukkitClass("net.minecraft.server.BlockActionContext");
                class_Block_getPlacedStateMethod = class_Block.getMethod("getPlacedState", class_BlockActionContext);
                Class<Enum> class_EnumHand = (Class<Enum>) fixBukkitClass("net.minecraft.server.EnumHand");
                enum_EnumHand_MAIN_HAND = Enum.valueOf(class_EnumHand, "MAIN_HAND");
                Class<?> class_MovingObjectPositionBlock = fixBukkitClass("net.minecraft.server.MovingObjectPositionBlock");
                class_BlockActionContext_constructor = class_BlockActionContext.getDeclaredConstructor(class_World, class_EntityHuman, class_EnumHand, class_ItemStack, class_MovingObjectPositionBlock);
                class_BlockActionContext_constructor.setAccessible(true);
                Class<?> class_Vec3D = fixBukkitClass("net.minecraft.server.Vec3D");
                class_MovingObjectPositionBlock_createMethod = class_MovingObjectPositionBlock.getMethod("a", class_Vec3D, class_EnumDirection, class_BlockPosition);
                if (!class_MovingObjectPositionBlock_createMethod.getReturnType().isAssignableFrom(class_MovingObjectPositionBlock)) {
                    throw new Exception("MovingObjectPositionBlock factory returns wrong type");
                }
                class_CraftBlock_setTypeAndDataMethod = class_CraftBlock.getMethod("setTypeAndData", class_IBlockData, Boolean.TYPE);
                class_nms_Block_getBlockDataMethod = class_Block.getMethod("getBlockData");
            } catch (Throwable ex) {
                class_CraftBlock = null;
                logger.log(Level.WARNING, "Could not bind to auto block state methods");
            }
        } catch (Throwable ex) {
            failed = true;
            logger.log(Level.SEVERE, "An unexpected error occurred initializing Magic", ex);
        }

        return !failed;
    }

    public static boolean getFailed() {
        return failed;
    }

    public static Class<?> getClass(String className) {
        Class<?> result = null;
        try {
            result = NMSUtils.class.getClassLoader().loadClass(className);
        } catch (Exception ex) {
            result = null;
        }

        return result;
    }

    public static Class<?> getBukkitClass(String className) {
        Class<?> result = null;
        try {
            result = fixBukkitClass(className);
        } catch (Exception ex) {
            result = null;
        }

        return result;
    }

    public static Class<?> fixBukkitClass(String className) throws ClassNotFoundException {
        if (!versionPrefix.isEmpty()) {
            className = className.replace("org.bukkit.craftbukkit.", "org.bukkit.craftbukkit." + versionPrefix);
            className = className.replace("net.minecraft.server.", "net.minecraft.server." + versionPrefix);
        }

        return NMSUtils.class.getClassLoader().loadClass(className);
    }

    public static Object getHandle(Server server) {
        Object handle = null;
        try {
            handle = class_CraftServer_getServerMethod.invoke(server);
        } catch (Throwable ex) {
            handle = null;
        }
        return handle;
    }

    public static Object getHandle(World world) {
        if (world == null) return null;
        Object handle = null;
        try {
            handle = class_CraftWorld_getHandleMethod.invoke(world);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    public static Object getHandle(Entity entity) {
        if (entity == null) return null;
        Object handle = null;
        try {
            handle = class_CraftEntity_getHandleMethod.invoke(entity);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    public static Object getHandle(LivingEntity entity) {
        if (entity == null) return null;
        Object handle = null;
        try {
            handle = class_CraftLivingEntity_getHandleMethod.invoke(entity);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    public static Object getHandle(Chunk chunk) {
        Object handle = null;
        try {
            handle = class_CraftChunk_getHandleMethod.invoke(chunk);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    public static Object getHandle(Player player) {
        Object handle = null;
        try {
            handle = class_CraftPlayer_getHandleMethod.invoke(player);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    protected static void sendPacket(Server server, Location source, Collection<? extends Player> players, Object packet) throws Exception {
        players = ((players != null && players.size() > 0) ? players : server.getOnlinePlayers());

        int viewDistance = Bukkit.getServer().getViewDistance() * 16;
        int viewDistanceSquared = viewDistance * viewDistance;
        World sourceWorld = source.getWorld();
        for (Player player : players) {
            Location location = player.getLocation();
            if (!location.getWorld().equals(sourceWorld)) continue;
            if (location.distanceSquared(source) <= viewDistanceSquared) {
                sendPacket(player, packet);
            }
        }
    }

    protected static void sendPacket(Player player, Object packet) throws Exception {
        Object playerHandle = getHandle(player);
        Field connectionField = playerHandle.getClass().getField("playerConnection");
        Object connection = connectionField.get(playerHandle);
        Method sendPacketMethod = connection.getClass().getMethod("sendPacket", class_Packet);
        sendPacketMethod.invoke(connection, packet);
    }

    public static boolean isPublic(Field field) {
        if (field == null) return false;
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers);
    }

}

