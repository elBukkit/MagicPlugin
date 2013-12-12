package com.elmakers.mine.bukkit.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;

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
		} 
		catch (Throwable ex) {
			ex.printStackTrace();
		}

		// This is version-dependent, so try both.
		try { 	
			class_PacketPlayOutMapChunkBulk = fixBukkitClass("net.minecraft.server.PacketPlayOutMapChunkBulk");
		} 
		catch (Throwable ex) {
		}
		try { 	
			class_Packet56MapChunkBulk = fixBukkitClass("net.minecraft.server.Packet56MapChunkBulk");
		} 
		catch (Throwable ex) {
		}
		
		if (class_PacketPlayOutMapChunkBulk == null && class_Packet56MapChunkBulk == null) {
			// This should probably use a logger, but.. this is a pretty bad issue.
			System.err.println("Could not bind to either PlayOutMapChunk packet version");
		}
	}

	protected static Class<?> fixBukkitClass(String className) {
		className = className.replace("org.bukkit.craftbukkit.", "org.bukkit.craftbukkit." + versionPrefix);
		className = className.replace("net.minecraft.server.", "net.minecraft.server." + versionPrefix);
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	protected static Object getHandle(org.bukkit.inventory.ItemStack stack) {
		Object handle = null;
		try {
			Field handleField = stack.getClass().getDeclaredField("handle");
			handleField.setAccessible(true);
			handle = handleField.get(stack);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return handle;
	}

	protected static Object getHandle(org.bukkit.World world) {
		Object handle = null;
		try {
			Method handleMethod = world.getClass().getMethod("getHandle");
			handle = handleMethod.invoke(world);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return handle;
	}

	protected static Object getHandle(org.bukkit.Chunk chunk) {
		Object handle = null;
		try {
			Method handleMethod = chunk.getClass().getMethod("getHandle");
			handle = handleMethod.invoke(chunk);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return handle;
	}

	protected static Object getHandle(org.bukkit.entity.Player player) {
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
}
