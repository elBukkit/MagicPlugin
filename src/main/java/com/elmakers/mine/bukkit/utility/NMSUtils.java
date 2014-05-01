package com.elmakers.mine.bukkit.utility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
	protected static String versionPrefix = "";

	protected static Class<?> class_ItemStack;
	protected static Class<?> class_NBTBase;
	protected static Class<?> class_NBTTagCompound;
	protected static Class<?> class_NBTTagList;
	protected static Class<?> class_CraftInventoryCustom;
	protected static Class<?> class_CraftItemStack;
	protected static Class<?> class_CraftLivingEntity;
	protected static Class<?> class_Entity;
	protected static Class<?> class_DataWatcher;
	protected static Class<?> class_World;
	protected static Class<?> class_Packet;
	protected static Class<Enum> class_EnumSkyBlock;
	protected static Class<?> class_PacketPlayOutMapChunkBulk;
	protected static Class<?> class_Packet56MapChunkBulk;
	protected static Class<?> class_Packet63WorldParticles;
	protected static Class<?> class_PacketPlayOutWorldParticles;
	protected static Class<?> class_EntityPainting;
	protected static Class<?> class_EntityItemFrame;

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
			class_ItemStack = fixBukkitClass("net.minecraft.server.ItemStack");
			class_DataWatcher = fixBukkitClass("net.minecraft.server.DataWatcher");
			class_NBTBase = fixBukkitClass("net.minecraft.server.NBTBase");
			class_NBTTagCompound = fixBukkitClass("net.minecraft.server.NBTTagCompound");
			class_NBTTagList = fixBukkitClass("net.minecraft.server.NBTTagList");
			class_CraftInventoryCustom = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftInventoryCustom");
			class_CraftItemStack = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftItemStack");
			class_CraftLivingEntity = fixBukkitClass("org.bukkit.craftbukkit.entity.CraftLivingEntity");
			class_Packet = fixBukkitClass("net.minecraft.server.Packet");
			class_World = fixBukkitClass("net.minecraft.server.World");
			class_EnumSkyBlock = (Class<Enum>)fixBukkitClass("net.minecraft.server.EnumSkyBlock");
			class_EntityPainting = fixBukkitClass("net.minecraft.server.EntityPainting");
			class_EntityItemFrame = fixBukkitClass("net.minecraft.server.EntityItemFrame");
		} 
		catch (Throwable ex) {
			ex.printStackTrace();
		}

		// These is version-dependent, so try both.
		class_PacketPlayOutMapChunkBulk = getBukkitClass("net.minecraft.server.PacketPlayOutMapChunkBulk");
		class_Packet56MapChunkBulk = getBukkitClass("net.minecraft.server.Packet56MapChunkBulk");
		if (class_PacketPlayOutMapChunkBulk == null && class_Packet56MapChunkBulk == null) {
			// This should probably use a logger, but.. this is a pretty bad issue.
			System.err.println("Could not bind to either PlayOutMapChunk packet version");
		}
		
		class_PacketPlayOutWorldParticles = getBukkitClass("net.minecraft.server.PacketPlayOutWorldParticles");
		class_Packet63WorldParticles = getBukkitClass("net.minecraft.server.Packet63WorldParticles");
		if (class_PacketPlayOutWorldParticles == null && class_Packet63WorldParticles == null) {
			// This should probably use a logger, but.. this is a pretty bad issue.
			System.err.println("Could not bind to either PlayOutWorldParticles packet version");
		}
	}
	
	public static Class<?> getBukkitClass(String className) {
		Class<?> ret = null;
		try { 	
			ret = fixBukkitClass(className);
		} 
		catch (Throwable ex) {
		}
		return ret;
	}
	
	public static Class<?> fixBukkitClass(String className) {
		className = className.replace("org.bukkit.craftbukkit.", "org.bukkit.craftbukkit." + versionPrefix);
		className = className.replace("net.minecraft.server.", "net.minecraft.server." + versionPrefix);
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
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
			
		for(Player p1 : players) {
			if(p1.getLocation().distanceSquared(source) <= Bukkit.getServer().getViewDistance() * Bukkit.getServer().getViewDistance()) {
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

	protected static Object getNMSCopy(ItemStack stack) {
    	Object nms = null;
    	try {
			Method copyMethod = class_CraftItemStack.getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
			nms = copyMethod.invoke(null, stack);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return nms;
    }

	protected static Object getTag(Object mcItemStack) {		
		Object tag = null;
		try {
			Field tagField = class_ItemStack.getField("tag");
			tag = tagField.get(mcItemStack);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return tag;
	}
	
	public static ItemStack getCopy(ItemStack stack) {
		if (stack == null) return null;
		
        try {
            Object craft = getNMSCopy(stack);
            Method mirrorMethod = class_CraftItemStack.getMethod("asCraftMirror", craft.getClass());
            stack = (ItemStack)mirrorMethod.invoke(null, craft);
        } catch (Throwable ex) {
            stack = null;
        }

        return stack;
	}
	
	public static ItemStack makeReal(ItemStack stack) {
		if (stack == null) return null;
		if (getHandle(stack) != null) return stack;
		
		return getCopy(stack);
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
			Method getMethod = class_NBTTagCompound.getMethod("get", String.class);
			meta = getMethod.invoke(tagObject, tag);
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
				Method setMethod = class_NBTTagCompound.getMethod("set", String.class, class_NBTBase);
				setMethod.invoke(tagObject, tag, outputObject);
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
			Method getStringMethod = class_NBTTagCompound.getMethod("getString", String.class);
			meta = (String)getStringMethod.invoke(node, tag);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return meta;
	}

	public static void setMeta(Object node, String tag, String value) {
		if (node == null|| !class_NBTTagCompound.isInstance(node)) return;
		try {
			if (value == null || value.length() == 0) {
				Method setStringMethod = class_NBTTagCompound.getMethod("remove", String.class);
				setStringMethod.invoke(node, tag);
			} else {
				Method setStringMethod = class_NBTTagCompound.getMethod("setString", String.class, String.class);
				setStringMethod.invoke(node, tag, value);
			}
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
			Method getStringMethod = class_NBTTagCompound.getMethod("getString", String.class);
			meta = (String)getStringMethod.invoke(tagObject, tag);
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
			Method setStringMethod = class_NBTTagCompound.getMethod("setString", String.class, String.class);
			setStringMethod.invoke(tagObject, tag, value);
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
			Method setMethod = class_NBTTagCompound.getMethod("set", String.class, class_NBTBase);		
			setMethod.invoke(tagObject, "ench", enchList);
		} catch (Throwable ex) {
			
		}
    }

	public static boolean createExplosion(Entity entity, World world, double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
		boolean result = false;
		try {
			Object worldHandle = getHandle(world);
			if (worldHandle == null) return false;
			Object entityHandle = entity == null ? null : getHandle(entity);
			 
			Method explodeMethod = class_World.getMethod("createExplosion", class_Entity, Double.TYPE, Double.TYPE, Double.TYPE, Float.TYPE, Boolean.TYPE, Boolean.TYPE);		
			Object explosion = explodeMethod.invoke(worldHandle, entityHandle, x, y, z, power, setFire, breakBlocks);
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
}
