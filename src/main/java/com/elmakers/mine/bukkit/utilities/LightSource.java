package com.elmakers.mine.bukkit.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.NMSUtils;

public class LightSource extends NMSUtils {
	/*
	 * This was modified by NathanWolf from the following code:
	 * 
	 * http://forums.bukkit.org/threads/resource-server-side-lighting-no-it-isnt-just-client-side.154503/
	 */
	
	public static void createLightSource (Location l, int level) {
		createLightSource(l, level, null);
	}
	
	/**
	 * Create light with level at a location. Players can be added to make them only see it.
	 * @param l
	 * @param level
	 * @param players
	 */
	@SuppressWarnings("unchecked")
	public static void createLightSource (Location l, int level, Collection<Player> players) {
		// Store the original light level
		// int oLevel = l.getBlock().getLightLevel();
		
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
		// ATTENTION: I've given up on making these temporary.
		/*
		try {
			//If you comment this out it is more likely to get light sources you can't remove
			// but if you do comment it, light is consistent on relog and what not.
			addLightMethod.invoke(worldHandle, blockEnum, l.getBlockX(), l.getBlockY(), l.getBlockZ(), oLevel);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		*/
	}
	
	public static void deleteLightSource (Location l) {
		deleteLightSource(l, null);
	}
	
	/**
	 * Updates the block making the light source return to what it actually is
	 * @param l
	 * @param players
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
	 * @param players
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
			} 
			int t = l.clone().add(0, 1, 0).getBlock().getTypeId();
			l.clone().add(0, 1, 0).getBlock().setTypeId(t == 1 ? 2 : 1);
			
			sendPacket(l, players, packet);
				
			l.clone().add(0, 1, 0).getBlock().setTypeId(t);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}