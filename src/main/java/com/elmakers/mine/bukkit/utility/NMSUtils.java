package com.elmakers.mine.bukkit.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import com.elmakers.mine.bukkit.block.Schematic;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Contains some raw methods for doing some simple NMS utilities.
 * 
 * This is not meant to be a replacement for full-on NMS or Protocol libs,
 * but it is enough for Magic to use internally without requiring any
 * external dependencies.
 * 
 * Use any of this at your own risk!
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class NMSUtils {
    protected static boolean failed = false;

    protected static String versionPrefix = "";
    protected static boolean isLegacy = false;

    protected static Class<?> class_ItemStack;
    protected static Class<?> class_NBTBase;
    protected static Class<?> class_NBTTagCompound;
    protected static Class<?> class_NBTTagList;
    protected static Class<?> class_NBTTagByte;
    protected static Class<?> class_NBTTagString;
    protected static Class<?> class_CraftTask;
    protected static Class<?> class_CraftInventoryCustom;
    protected static Class<?> class_CraftItemStack;
    protected static Class<?> class_CraftLivingEntity;
    protected static Class<?> class_Entity;
    protected static Class<?> class_EntityCreature;
    protected static Class<?> class_EntityLiving;
    protected static Class<?> class_DataWatcher;
    protected static Class<?> class_DamageSource;
    protected static Class<?> class_World;
    protected static Class<?> class_Packet;
    protected static Class<Enum> class_EnumSkyBlock;
    protected static Class<?> class_PacketPlayOutMapChunkBulk;
    protected static Class<?> class_EntityPainting;
    protected static Class<?> class_EntityItemFrame;
    protected static Class<?> class_EntityMinecartRideable;
    protected static Class<?> class_EntityTNTPrimed;
    protected static Class<?> class_AxisAlignedBB;
    protected static Class<?> class_PathPoint;
    protected static Class<?> class_PathEntity;
    protected static Class<?> class_EntityFirework;
    protected static Class<?> class_CraftSkull;
    protected static Class<?> class_CraftBanner;
    protected static Class<?> class_CraftMetaSkull;
    protected static Class<?> class_CraftMetaBanner;
    protected static Class<?> class_GameProfile;
    protected static Class<?> class_GameProfileProperty;
    protected static Class<?> class_BlockPosition;
    protected static Class<?> class_NBTCompressedStreamTools;
    protected static Class<Enum> class_EnumDirection;

    protected static Method class_NBTTagList_addMethod;
    protected static Method class_NBTTagCompound_setMethod;
    protected static Method class_DataWatcher_watchMethod;
    protected static Method class_World_getEntitiesMethod;
    protected static Method class_Entity_getBukkitEntityMethod;
    protected static Method class_EntityLiving_damageEntityMethod;
    protected static Method class_DamageSource_getMagicSourceMethod;
    protected static Method class_AxisAlignedBB_createBBMethod;
    protected static Method class_World_explodeMethod;
    protected static Method class_NBTTagCompound_setBooleanMethod;
    protected static Method class_NBTTagCompound_setStringMethod;
    protected static Method class_NBTTagCompound_removeMethod;
    protected static Method class_NBTTagCompound_getStringMethod;
    protected static Method class_NBTTagCompound_getMethod;
    protected static Method class_NBTTagCompound_getCompoundMethod;
    protected static Method class_NBTTagCompound_getShortMethod;
    protected static Method class_NBTTagCompound_getByteArrayMethod;
    protected static Method class_World_addEntityMethod;
    protected static Method class_CraftMetaBanner_getPatternsMethod;
    protected static Method class_CraftMetaBanner_setPatternsMethod;
    protected static Method class_CraftMetaBanner_getBaseColorMethod;
    protected static Method class_CraftMetaBanner_setBaseColorMethod;
    protected static Method class_CraftBanner_getPatternsMethod;
    protected static Method class_CraftBanner_setPatternsMethod;
    protected static Method class_CraftBanner_getBaseColorMethod;
    protected static Method class_CraftBanner_setBaseColorMethod;
    protected static Method class_NBTCompressedStreamTools_loadFileMethod;

    protected static Method class_CraftItemStack_copyMethod;
    protected static Method class_CraftItemStack_mirrorMethod;
    protected static Method class_NBTTagCompound_hasKeyMethod;

    protected static Constructor class_NBTTagList_consructor;
    protected static Constructor class_NBTTagList_legacy_consructor;
    protected static Constructor class_CraftInventoryCustom_constructor;
    protected static Constructor class_NBTTagByte_constructor;
    protected static Constructor class_NBTTagByte_legacy_constructor;
    protected static Constructor class_EntityFireworkConstructor;
    protected static Constructor class_EntityPaintingConstructor;
    protected static Constructor class_EntityItemFrameConstructor;
    protected static Constructor class_BlockPositionConstructor;

    protected static Field class_Entity_invulnerableField;
    protected static Field class_Entity_motXField;
    protected static Field class_Entity_motYField;
    protected static Field class_Entity_motZField;
    protected static Field class_ItemStack_tagField;
    protected static Field class_DamageSource_MagicField;
    protected static Field class_Firework_ticksFlownField;
    protected static Field class_Firework_expectedLifespanField;
    protected static Field class_CraftSkull_profile;
    protected static Field class_CraftMetaSkull_profile;
    protected static Field class_GameProfile_properties;
    protected static Field class_GameProfileProperty_value;
    protected static Field class_ItemStack_count;
    protected static Field class_EntityTNTPrimed_source;

    static
    {
        // Find classes Bukkit hides from us. :-D
        // Much thanks to @DPOHVAR for sharing the PowerNBT code that powers the reflection approach.
        String className = Bukkit.getServer().getClass().getName();
        String[] packages = className.split("\\.");
        if (packages.length == 5) {
            versionPrefix = packages[3] + ".";
        }

        try {
            class_Entity = fixBukkitClass("net.minecraft.server.Entity");
            class_EntityLiving = fixBukkitClass("net.minecraft.server.EntityLiving");
            class_ItemStack = fixBukkitClass("net.minecraft.server.ItemStack");
            class_DataWatcher = fixBukkitClass("net.minecraft.server.DataWatcher");
            class_NBTBase = fixBukkitClass("net.minecraft.server.NBTBase");
            class_NBTTagCompound = fixBukkitClass("net.minecraft.server.NBTTagCompound");
            class_NBTTagList = fixBukkitClass("net.minecraft.server.NBTTagList");
            class_NBTTagString = fixBukkitClass("net.minecraft.server.NBTTagString");
            class_NBTTagByte = fixBukkitClass("net.minecraft.server.NBTTagByte");
            class_CraftInventoryCustom = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftInventoryCustom");
            class_CraftItemStack = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftItemStack");
            class_CraftTask = fixBukkitClass("org.bukkit.craftbukkit.scheduler.CraftTask");
            class_CraftLivingEntity = fixBukkitClass("org.bukkit.craftbukkit.entity.CraftLivingEntity");
            class_Packet = fixBukkitClass("net.minecraft.server.Packet");
            class_World = fixBukkitClass("net.minecraft.server.World");
            class_EnumSkyBlock = (Class<Enum>)fixBukkitClass("net.minecraft.server.EnumSkyBlock");
            class_EntityPainting = fixBukkitClass("net.minecraft.server.EntityPainting");
            class_EntityCreature = fixBukkitClass("net.minecraft.server.EntityCreature");
            class_EntityItemFrame = fixBukkitClass("net.minecraft.server.EntityItemFrame");
            class_EntityMinecartRideable = fixBukkitClass("net.minecraft.server.EntityMinecartRideable");
            class_EntityTNTPrimed = fixBukkitClass("net.minecraft.server.EntityTNTPrimed");
            class_AxisAlignedBB = fixBukkitClass("net.minecraft.server.AxisAlignedBB");
            class_DamageSource = fixBukkitClass("net.minecraft.server.DamageSource");
            class_PathEntity = fixBukkitClass("net.minecraft.server.PathEntity");
            class_PathPoint = fixBukkitClass("net.minecraft.server.PathPoint");
            class_EntityFirework = fixBukkitClass("net.minecraft.server.EntityFireworks");
            class_CraftSkull = fixBukkitClass("org.bukkit.craftbukkit.block.CraftSkull");
            class_CraftMetaSkull = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftMetaSkull");
            class_NBTCompressedStreamTools = fixBukkitClass("net.minecraft.server.NBTCompressedStreamTools");

            class_NBTTagList_addMethod = class_NBTTagList.getMethod("add", class_NBTBase);
            class_NBTTagCompound_setMethod = class_NBTTagCompound.getMethod("set", String.class, class_NBTBase);
            class_DataWatcher_watchMethod = class_DataWatcher.getMethod("watch", Integer.TYPE, Object.class);
            class_World_getEntitiesMethod = class_World.getMethod("getEntities", class_Entity, class_AxisAlignedBB);
            class_Entity_getBukkitEntityMethod = class_Entity.getMethod("getBukkitEntity");
            class_AxisAlignedBB_createBBMethod = class_AxisAlignedBB.getMethod("a", Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE);
            class_World_explodeMethod = class_World.getMethod("createExplosion", class_Entity, Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Boolean.TYPE, Boolean.TYPE);
            class_NBTTagCompound_setBooleanMethod = class_NBTTagCompound.getMethod("setBoolean", String.class, Boolean.TYPE);
            class_NBTTagCompound_setStringMethod = class_NBTTagCompound.getMethod("setString", String.class, String.class);
            class_NBTTagCompound_removeMethod = class_NBTTagCompound.getMethod("remove", String.class);
            class_NBTTagCompound_getStringMethod = class_NBTTagCompound.getMethod("getString", String.class);
            class_NBTTagCompound_getShortMethod = class_NBTTagCompound.getMethod("getShort", String.class);
            class_NBTTagCompound_getByteArrayMethod = class_NBTTagCompound.getMethod("getByteArray", String.class);
            class_CraftItemStack_copyMethod = class_CraftItemStack.getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
            class_CraftItemStack_mirrorMethod = class_CraftItemStack.getMethod("asCraftMirror", class_ItemStack);
            class_NBTTagCompound_hasKeyMethod = class_NBTTagCompound.getMethod("hasKey", String.class);
            class_NBTTagCompound_getMethod = class_NBTTagCompound.getMethod("get", String.class);
            class_NBTTagCompound_getCompoundMethod = class_NBTTagCompound.getMethod("getCompound", String.class);
            class_EntityLiving_damageEntityMethod = class_EntityLiving.getMethod("damageEntity", class_DamageSource, Float.TYPE);
            class_DamageSource_getMagicSourceMethod = class_DamageSource.getMethod("b", class_Entity, class_Entity);
            class_World_addEntityMethod = class_World.getMethod("addEntity", class_Entity, CreatureSpawnEvent.SpawnReason.class);
            class_NBTCompressedStreamTools_loadFileMethod = class_NBTCompressedStreamTools.getMethod("a", InputStream.class);

            class_CraftInventoryCustom_constructor = class_CraftInventoryCustom.getConstructor(InventoryHolder.class, Integer.TYPE, String.class);
            class_EntityFireworkConstructor = class_EntityFirework.getConstructor(class_World, Double.TYPE, Double.TYPE, Double.TYPE, class_ItemStack);

            class_Entity_invulnerableField = class_Entity.getDeclaredField("invulnerable");
            class_Entity_invulnerableField.setAccessible(true);
            class_Entity_motXField = class_Entity.getDeclaredField("motX");
            class_Entity_motXField.setAccessible(true);
            class_Entity_motYField = class_Entity.getDeclaredField("motY");
            class_Entity_motYField.setAccessible(true);
            class_Entity_motZField = class_Entity.getDeclaredField("motZ");
            class_Entity_motZField.setAccessible(true);
            class_ItemStack_tagField = class_ItemStack.getDeclaredField("tag");
            class_ItemStack_tagField.setAccessible(true);
            class_DamageSource_MagicField = class_DamageSource.getField("MAGIC");
            class_EntityTNTPrimed_source = class_EntityTNTPrimed.getDeclaredField("source");
            class_EntityTNTPrimed_source.setAccessible(true);

            class_Firework_ticksFlownField = class_EntityFirework.getDeclaredField("ticksFlown");
            class_Firework_ticksFlownField.setAccessible(true);
            class_Firework_expectedLifespanField = class_EntityFirework.getDeclaredField("expectedLifespan");
            class_Firework_expectedLifespanField.setAccessible(true);

            class_NBTTagList_consructor = class_NBTTagString.getConstructor(String.class);
            class_NBTTagByte_constructor = class_NBTTagByte.getConstructor(Byte.TYPE);
            class_ItemStack_count = class_ItemStack.getDeclaredField("count");
            class_ItemStack_count.setAccessible(true);

            isLegacy = false;
            try {
                class_GameProfile = getClass("com.mojang.authlib.GameProfile");
                class_GameProfileProperty = getClass("com.mojang.authlib.properties.Property");
                class_CraftSkull_profile = class_CraftSkull.getDeclaredField("profile");
                class_CraftSkull_profile.setAccessible(true);
                class_CraftMetaSkull_profile = class_CraftMetaSkull.getDeclaredField("profile");
                class_CraftMetaSkull_profile.setAccessible(true);
                class_GameProfile_properties = class_GameProfile.getDeclaredField("properties");
                class_GameProfile_properties.setAccessible(true);
                class_GameProfileProperty_value = class_GameProfileProperty.getDeclaredField("value");
                class_GameProfileProperty_value.setAccessible(true);

                class_CraftMetaBanner = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftMetaBanner");
                class_CraftMetaBanner_getBaseColorMethod = class_CraftMetaBanner.getMethod("getBaseColor");
                class_CraftMetaBanner_getPatternsMethod = class_CraftMetaBanner.getMethod("getPatterns");
                class_CraftMetaBanner_setPatternsMethod = class_CraftMetaBanner.getMethod("setPatterns", List.class);
                class_CraftMetaBanner_setBaseColorMethod = class_CraftMetaBanner.getMethod("setBaseColor", DyeColor.class);

                class_CraftBanner = fixBukkitClass("org.bukkit.craftbukkit.block.CraftBanner");
                class_CraftBanner_getBaseColorMethod = class_CraftBanner.getMethod("getBaseColor");
                class_CraftBanner_getPatternsMethod = class_CraftBanner.getMethod("getPatterns");
                class_CraftBanner_setPatternsMethod = class_CraftBanner.getMethod("setPatterns", List.class);
                class_CraftBanner_setBaseColorMethod = class_CraftBanner.getMethod("setBaseColor", DyeColor.class);

                class_BlockPosition = fixBukkitClass("net.minecraft.server.BlockPosition");
                class_EnumDirection = (Class<Enum>)fixBukkitClass("net.minecraft.server.EnumDirection");
                class_BlockPositionConstructor = class_BlockPosition.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE);
                class_EntityPaintingConstructor = class_EntityPainting.getConstructor(class_World, class_BlockPosition, class_EnumDirection);
                class_EntityItemFrameConstructor = class_EntityItemFrame.getConstructor(class_World, class_BlockPosition, class_EnumDirection);
            }
            catch (Throwable legacy) {
                isLegacy = true;
                class_EntityPaintingConstructor = class_EntityPainting.getConstructor(class_World, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                class_EntityItemFrameConstructor = class_EntityItemFrame.getConstructor(class_World, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            }

            class_PacketPlayOutMapChunkBulk = getVersionedBukkitClass("net.minecraft.server.PacketPlayOutMapChunkBulk", "net.minecraft.server.Packet56MapChunkBulk");
        }
        catch (Throwable ex) {
            failed = true;
            ex.printStackTrace();
        }
    }

    public static boolean getFailed() {
        return failed;
    }

    public static boolean isLegacy() {
        return isLegacy;
    }


    public static Class<?> getVersionedBukkitClass(String newVersion, String oldVersion) {
        Class<?> c = getBukkitClass(newVersion);
        if (c == null) {
            c = getBukkitClass(oldVersion);
            if (c == null) {
                Bukkit.getLogger().warning("Could not bind to " + newVersion + " or " + oldVersion);
            }
        }
        return c;
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

    public static Object getHandle(org.bukkit.inventory.ItemStack stack) {
        Object handle = null;
        try {
            Field handleField = stack.getClass().getDeclaredField("handle");
            handleField.setAccessible(true);
            handle = handleField.get(stack);
        } catch (Throwable ex) {
            handle = null;
        }
        return handle;
    }

    public static Object getHandle(org.bukkit.World world) {
        if (world == null) return null;
        Object handle = null;
        try {
            Method handleMethod = world.getClass().getMethod("getHandle");
            handle = handleMethod.invoke(world);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    public static Object getHandle(org.bukkit.entity.Entity entity) {
        if (entity == null) return null;
        Object handle = null;
        try {
            Method handleMethod = entity.getClass().getMethod("getHandle");
            handle = handleMethod.invoke(entity);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    public static Object getHandle(org.bukkit.entity.LivingEntity entity) {
        if (entity == null) return null;
        Object handle = null;
        try {
            Method handleMethod = entity.getClass().getMethod("getHandle");
            handle = handleMethod.invoke(entity);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    public static boolean isDone(org.bukkit.Chunk chunk) {
        Object chunkHandle = getHandle(chunk);
        boolean done = false;
        try {
            Field doneField = chunkHandle.getClass().getDeclaredField("done");
            doneField.setAccessible(true);
            done = (Boolean)doneField.get(chunkHandle);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return done;
    }

    public static Object getHandle(org.bukkit.Chunk chunk) {
        Object handle = null;
        try {
            Method handleMethod = chunk.getClass().getMethod("getHandle");
            handle = handleMethod.invoke(chunk);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    public static Object getHandle(org.bukkit.entity.Player player) {
        Object handle = null;
        try {
            Method handleMethod = player.getClass().getMethod("getHandle");
            handle = handleMethod.invoke(player);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    protected static Object getHandle(Object object) {
        Object handle = null;
        try {
            Method handleMethod = object.getClass().getMethod("getHandle");
            handle = handleMethod.invoke(object);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return handle;
    }

    protected static void sendPacket(Location source, Collection<Player> players, Object packet) throws Exception  {
        players = ((players != null && players.size() > 0) ? players : source.getWorld().getPlayers());

        int viewDistanceSquared = Bukkit.getServer().getViewDistance() * Bukkit.getServer().getViewDistance();
        for(Player p1 : players) {
            if(p1.getLocation().distanceSquared(source) <= viewDistanceSquared) {
                sendPacket(p1, packet);
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
    
    public static int getFacing(BlockFace direction)
    {
        int dir;
        switch (direction) {
        case SOUTH:
        default:
            dir = 0;
            break;
        case WEST:
            dir = 1;
            break;
        case NORTH:
            dir = 2;
            break;
        case EAST:
            dir = 3;
            break;
        }
        
        return dir;
    }

    public static org.bukkit.entity.Entity getBukkitEntity(Object entity)
    {
        if (entity == null) return null;
        try {
            Method getMethod = entity.getClass().getMethod("getBukkitEntity");
            Object bukkitEntity = getMethod.invoke(entity);
            if (!(bukkitEntity instanceof org.bukkit.entity.Entity)) return null;
            return (org.bukkit.entity.Entity)bukkitEntity;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Object getTag(Object mcItemStack) {
        Object tag = null;
        try {
            tag = class_ItemStack_tagField.get(mcItemStack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return tag;
    }

    protected static Object getNMSCopy(ItemStack stack) {
        Object nms = null;
        try {
            nms = class_CraftItemStack_copyMethod.invoke(null, stack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return nms;
    }

    public static ItemStack getCopy(ItemStack stack) {
        if (stack == null) return null;

        try {
            Object craft = getNMSCopy(stack);
            stack = (ItemStack)class_CraftItemStack_mirrorMethod.invoke(null, craft);
        } catch (Throwable ex) {
            stack = null;
        }

        return stack;
    }

    public static ItemStack makeReal(ItemStack stack) {
        if (stack == null) return null;
        Object nmsStack = getHandle(stack);
        if (nmsStack == null) {
            stack = getCopy(stack);
            nmsStack = getHandle(stack);
        }
        if (nmsStack == null) {
            return null;
        }
        try {
            Object tag = class_ItemStack_tagField.get(nmsStack);
            if (tag == null) {
                class_ItemStack_tagField.set(nmsStack, class_NBTTagCompound.newInstance());
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }

        return stack;
    }

    public static String getMeta(ItemStack stack, String tag, String defaultValue) {
        String result = getMeta(stack, tag);
        return result == null ? defaultValue : result;
    }

    public static boolean hasMeta(ItemStack stack, String tag) {
        return getNode(stack, tag) != null;
    }

    public static Object getNode(ItemStack stack, String tag) {
        if (stack == null) return null;
        Object meta = null;
        try {
            Object craft = getHandle(stack);
            if (craft == null) return null;
            Object tagObject = getTag(craft);
            if (tagObject == null) return null;
            meta = class_NBTTagCompound_getMethod.invoke(tagObject, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    public static boolean containsNode(Object nbtBase, String tag) {
        if (nbtBase == null) return false;
        Boolean result = false;
        try {
            result = (Boolean)class_NBTTagCompound_hasKeyMethod.invoke(nbtBase, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static Object getNode(Object nbtBase, String tag) {
        if (nbtBase == null) return null;
        Object meta = null;
        try {
            meta = class_NBTTagCompound_getMethod.invoke(nbtBase, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    public static Object createNode(Object nbtBase, String tag) {
        if (nbtBase == null) return null;
        Object meta = null;
        try {
            meta = class_NBTTagCompound_getCompoundMethod.invoke(nbtBase, tag);
            class_NBTTagCompound_setMethod.invoke(nbtBase, tag, meta);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    public static Object createNode(ItemStack stack, String tag) {
        if (stack == null) return null;
        Object outputObject = getNode(stack, tag);
        if (outputObject == null) {
            try {
                Object craft = getHandle(stack);
                if (craft == null) return null;
                Object tagObject = getTag(craft);
                if (tagObject == null) return null;
                outputObject = class_NBTTagCompound.newInstance();
                class_NBTTagCompound_setMethod.invoke(tagObject, tag, outputObject);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        return outputObject;
    }

    public static String getMeta(Object node, String tag, String defaultValue) {
        String meta = getMeta(node, tag);
        return meta == null || meta.length() == 0 ? defaultValue : meta;
    }

    public static String getMeta(Object node, String tag) {
        if (node == null || !class_NBTTagCompound.isInstance(node)) return null;
        String meta = null;
        try {
            meta = (String)class_NBTTagCompound_getStringMethod.invoke(node, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    public static void setMeta(Object node, String tag, String value) {
        if (node == null|| !class_NBTTagCompound.isInstance(node)) return;
        try {
            if (value == null || value.length() == 0) {
                class_NBTTagCompound_removeMethod.invoke(node, tag);
            } else {
                class_NBTTagCompound_setStringMethod.invoke(node, tag, value);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void removeMeta(Object node, String tag) {
        if (node == null|| !class_NBTTagCompound.isInstance(node)) return;
        try {
            class_NBTTagCompound_removeMethod.invoke(node, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void removeMeta(ItemStack stack, String tag) {
        if (stack == null) return;

        try {
            Object craft = getHandle(stack);
            if (craft == null) return;
            Object tagObject = getTag(craft);
            if (tagObject == null) return;
            removeMeta(tagObject, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static String getMeta(ItemStack stack, String tag) {
        if (stack == null) return null;
        String meta = null;
        try {
            Object craft = getHandle(stack);
            if (craft == null) return null;
            Object tagObject = getTag(craft);
            if (tagObject == null) return null;
            meta = (String)class_NBTTagCompound_getStringMethod.invoke(tagObject, tag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return meta;
    }

    public static void setMeta(ItemStack stack, String tag, String value) {
        if (stack == null) return;
        try {
            Object craft = getHandle(stack);
            if (craft == null) return;
            Object tagObject = getTag(craft);
            if (tagObject == null) return;
            class_NBTTagCompound_setStringMethod.invoke(tagObject, tag, value);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void addGlow(ItemStack stack) {
        if (stack == null) return;

        try {
            Object craft = getHandle(stack);
            if (craft == null) return;
            Object tagObject = getTag(craft);
            if (tagObject == null) return;
            final Object enchList = class_NBTTagList.newInstance();
            class_NBTTagCompound_setMethod.invoke(tagObject, "ench", enchList);

            // Testing Glow API based on ItemMetadata storage
            Object bukkitData = createNode(stack, "bukkit");
            class_NBTTagCompound_setBooleanMethod.invoke(bukkitData, "glow", true);
        } catch (Throwable ex) {

        }
    }

    public static void removeGlow(ItemStack stack) {
        if (stack == null) return;

        Collection<Enchantment> enchants = stack.getEnchantments().keySet();
        for (Enchantment enchant : enchants) {
            stack.removeEnchantment(enchant);
        }

        try {
            Object craft = getHandle(stack);
            if (craft == null) return;
            Object tagObject = getTag(craft);
            if (tagObject == null) return;

            // Testing Glow API based on ItemMetadata storage
            Object bukkitData = getNode(stack, "bukkit");
            if (bukkitData != null) {
                class_NBTTagCompound_setBooleanMethod.invoke(bukkitData, "glow", false);
            }
        } catch (Throwable ex) {

        }
    }

    public static void makeUnbreakable(ItemStack stack) {
        if (stack == null) return;

        try {
            Object craft = getHandle(stack);
            if (craft == null) return;
            Object tagObject = getTag(craft);
            if (tagObject == null) return;

            Object unbreakableFlag = null;
            if (class_NBTTagByte_constructor != null) {
                unbreakableFlag = class_NBTTagByte_constructor.newInstance((byte) 1);
            } else {
                unbreakableFlag = class_NBTTagByte_legacy_constructor.newInstance("", (byte) 1);
            }
            class_NBTTagCompound_setMethod.invoke(tagObject, "Unbreakable", unbreakableFlag);
        } catch (Throwable ex) {

        }
    }

    public static void removeUnbreakable(ItemStack stack) {
        removeMeta(stack, "Unbreakable");
    }

    public static void hideFlags(ItemStack stack) {
        if (stack == null) return;

        try {
            Object craft = getHandle(stack);
            if (craft == null) return;
            Object tagObject = getTag(craft);
            if (tagObject == null) return;

            Object hideFlag = null;
            if (class_NBTTagByte_constructor != null) {
                hideFlag = class_NBTTagByte_constructor.newInstance((byte) 63);
            } else {
                hideFlag = class_NBTTagByte_legacy_constructor.newInstance("", (byte) 63);
            }
            class_NBTTagCompound_setMethod.invoke(tagObject, "HideFlags", hideFlag);
        } catch (Throwable ex) {

        }
    }

    public static boolean createExplosion(Entity entity, World world, double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        boolean result = false;
        if (world == null) return false;
        try {
            Object worldHandle = getHandle(world);
            if (worldHandle == null) return false;
            Object entityHandle = entity == null ? null : getHandle(entity);

            Object explosion = class_World_explodeMethod.invoke(worldHandle, entityHandle, x, y, z, power, setFire, breakBlocks);
            Field cancelledField = explosion.getClass().getDeclaredField("wasCanceled");
            result = (Boolean)cancelledField.get(explosion);
        } catch (Throwable ex) {
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    public static void makeTemporary(ItemStack itemStack, String message) {
        setMeta(itemStack, "temporary", message);
    }

    public static boolean isTemporary(ItemStack itemStack) {
        return hasMeta(itemStack, "temporary");
    }

    public static String getTemporaryMessage(ItemStack itemStack) {
        return getMeta(itemStack, "temporary");
    }

    public static void setReplacement(ItemStack itemStack, ItemStack replacement) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("item", replacement);
        setMeta(itemStack, "replacement", configuration.saveToString());
    }

    public static ItemStack getReplacement(ItemStack itemStack) {
        String serialized = getMeta(itemStack, "replacement");
        if (serialized == null || serialized.isEmpty()) {
            return null;
        }
        YamlConfiguration configuration = new YamlConfiguration();
        ItemStack replacement = null;
        try {
            configuration.loadFromString(serialized);
            replacement = configuration.getItemStack("item");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return replacement;
    }

    protected static Object getTagString(String value) {
        try {
            if (class_NBTTagList_legacy_consructor != null) {
                return class_NBTTagList_legacy_consructor.newInstance("", value);
            }
            return class_NBTTagList_consructor.newInstance(value);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return null;
    }

    public static Object setStringList(Object nbtBase, String tag, Collection<String> values) {
        if (nbtBase == null) return null;
        Object listMeta = null;
        try {
            listMeta = class_NBTTagList.newInstance();

            for (String value : values) {
                Object nbtString = getTagString(value);
                class_NBTTagList_addMethod.invoke(listMeta, nbtString);
            }

            class_NBTTagCompound_setMethod.invoke(nbtBase, tag, listMeta);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
        return listMeta;
    }

    public static boolean hasBannerSupport() {
        return !isLegacy;
    }

    public static boolean hasURLSkullSupport() {
        return !isLegacy;
    }

    public static Schematic loadSchematic(File inputFile) {
        if (inputFile == null || !inputFile.exists()) {
            return null;
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(inputFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return loadSchematic(inputStream);
    }

    public static Schematic loadSchematic(InputStream input) {
        if (input == null) return null;

        try {
            Object nbtData = class_NBTCompressedStreamTools_loadFileMethod.invoke(null, input);
            if (nbtData == null) {
                return null;
            }
            short width = (Short)class_NBTTagCompound_getShortMethod.invoke(nbtData, "Width");
            short height = (Short)class_NBTTagCompound_getShortMethod.invoke(nbtData, "Height");
            short length = (Short)class_NBTTagCompound_getShortMethod.invoke(nbtData, "Length");

            byte[] blocks = (byte[])class_NBTTagCompound_getByteArrayMethod.invoke(nbtData, "Blocks");
            byte[] data = (byte[])class_NBTTagCompound_getByteArrayMethod.invoke(nbtData, "Data");

            return new Schematic(width, height, length, blocks, data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
