package com.elmakers.mine.bukkit.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LightSource {
	/*
	 * This was modified by NathanWolf from the following code:
	 * 
	 * MINI README
	 * 
	 * This is free and you can use it/change it all you want.
	 * 
	 * There is a bukkit forum post on for this code:
	 * http://forums.bukkit.org/threads/resource-server-side-lighting-no-it-isnt-just-client-side.154503/
	 */
	
	private static String versionPrefix = "";

	private static Class<?> class_World;
	private static Class<?> class_Packet;
	private static Class<Enum> class_EnumSkyBlock;
	private static Class<?> class_PacketPlayOutMapChunkBulk;
	private static Class<?> class_Packet56MapChunkBulk;
	
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

	private static Class<?> fixBukkitClass(String className) throws Exception {
		className = className.replace("org.bukkit.craftbukkit.", "org.bukkit.craftbukkit." + versionPrefix);
		className = className.replace("net.minecraft.server.", "net.minecraft.server." + versionPrefix);
		return Class.forName(className);
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
	
	public static void createLightSource (Location l, int level) {
		createLightSource(l, level, null);
	}
	
	/**
	 * Create light with level at a location. Players can be added to make them only see it.
	 * @param l
	 * @param level
	 * @param players
	 */
	public static void createLightSource (Location l, int level, Collection<Player> players) {
		// Store the original light level
		int oLevel = l.getBlock().getLightLevel();
		
		// Sets the light source at the location to the level
		Enum<?> blockEnum = Enum.valueOf(class_EnumSkyBlock, "Block");
		
		// This hasn't changed or been deobfuscated since 1.5, so .. hopefully safe.
		Method addLightMethod = null;
		Object worldHandle = getHandle(l.getWorld());
		try {
			addLightMethod = class_World.getMethod("b", class_EnumSkyBlock, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);			
		} catch (Throwable ex) {
			addLightMethod = null;
			ex.printStackTrace();
		}
		
		if (worldHandle == null || addLightMethod == null) return;
		
		try {
			addLightMethod.invoke(worldHandle, blockEnum, l.getBlockX(), l.getBlockY(), l.getBlockZ(), level);
			
			// Send packets to the area telling players to see this level
			updateChunk(l, players);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		// Do this separately to make sure we don't permanently set the light if something's gone wrong.
		try {
			//If you comment this out it is more likely to get light sources you can't remove
			// but if you do comment it, light is consistent on relog and what not.
			addLightMethod.invoke(worldHandle, blockEnum, l.getBlockX(), l.getBlockY(), l.getBlockZ(), oLevel);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
	
	public static void deleteLightSource (Location l) {
		deleteLightSource(l, null);
	}
	
	/**
	 * Updates the block making the light source return to what it actually is
	 * @param l
	 */
	@SuppressWarnings("deprecation")
	public static void deleteLightSource (Location l, Collection<Player> players) {
		int t = l.getBlock().getTypeId();
		l.getBlock().setTypeId(t == 1 ? 2 : 1);
		
		updateChunk(l, players);
		
		l.getBlock().setTypeId(t);
	}
	
	/**
	 * Gets all the chunks touching/diagonal to the chunk the location is in and updates players with them.
	 * @param l
	 */
	@SuppressWarnings("deprecation")
	private static void updateChunk (Location l, Collection<Player> players) {
		// Make a list of NMS Chunks
		try {
			List<Object> chunks = new ArrayList<Object>();
			
			for (int x=-1; x<=1; x++) {
				for (int z=-1; z<=1; z++) {
					chunks.add(getHandle(l.clone().add(16 * x, 0, 16 * z).getChunk()));
				}
			}
	
			Object packet = null;
			// This is the 1.7.2 version of this packet.
			if (class_PacketPlayOutMapChunkBulk != null) {
				Constructor<?> packetConstructor = class_PacketPlayOutMapChunkBulk.getConstructor(List.class);
				packet = packetConstructor.newInstance(chunks);			
			} else {
				// Fall back to previous versions
				Constructor<?> packetConstructor = class_Packet56MapChunkBulk.getConstructor(List.class);
				packet = packetConstructor.newInstance(chunks);						
			}
			int t = l.clone().add(0, 1, 0).getBlock().getTypeId();
			l.clone().add(0, 1, 0).getBlock().setTypeId(t == 1 ? 2 : 1);
			
			players = ((players != null && players.size() > 0) ? players : l.getWorld().getPlayers());
				
			for(Player p1 : players) {
				if(p1.getLocation().distance(l) <= Bukkit.getServer().getViewDistance() * 16) {
					Object playerHandle = getHandle(p1);
					Field connectionField = playerHandle.getClass().getField("playerConnection");
					Object connection = connectionField.get(playerHandle);
					
					
					Method sendPacketMethod = connection.getClass().getMethod("sendPacket", class_Packet);
					sendPacketMethod.invoke(connection, packet);
				}
			}
				
			l.clone().add(0, 1, 0).getBlock().setTypeId(t);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}