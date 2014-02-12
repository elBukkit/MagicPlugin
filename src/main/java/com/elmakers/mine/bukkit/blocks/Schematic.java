package com.elmakers.mine.bukkit.blocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class Schematic {
	private static Class<?> vectorClass;
	private static Class<?> cuboidClipboardClass;
	private static Class<?> blockClass;
	private static Class<?> signClass;
	private static Class<?> compoundTagClass;
	private static Constructor<?> signConstructor;
	private static Constructor<?> vectorConstructor;
	private static Method getBlockMethod;
	private static Method getIdMethod;
	private static Method getDataMethod;
	private static Method getSizeMethod;
	private static Method getBlockXMethod;
	private static Method getBlockYMethod;
	private static Method getBlockZMethod;
	private static Method getLinesMethod;
	private static Method getNBTDataMethod;
	private static Method hasNBTDataMethod;
	private static Method signSetNBTDataMethod;
	
	private final Object weSchematic;
	private Vector center;
	private Vector size;
	private static Boolean classesValid = null;
	
	private static boolean checkClasses() {
		if (classesValid != null) {
			return classesValid;
		}
		try {
			vectorClass = Class.forName("com.sk89q.worldedit.Vector");
			if (vectorClass != null) {
				vectorConstructor = vectorClass.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
				getBlockXMethod = vectorClass.getMethod("getBlockX");				
				getBlockYMethod = vectorClass.getMethod("getBlockY");				
				getBlockZMethod = vectorClass.getMethod("getBlockZ");
			}
			cuboidClipboardClass = Class.forName("com.sk89q.worldedit.CuboidClipboard");
			if (cuboidClipboardClass != null) {
				getBlockMethod = cuboidClipboardClass.getMethod("getBlock", vectorClass);
				getSizeMethod = cuboidClipboardClass.getMethod("getSize");				
			}
			compoundTagClass = Class.forName("com.sk89q.jnbt.CompoundTag");
			blockClass = Class.forName("com.sk89q.worldedit.foundation.Block");
			if (blockClass != null) {
				getNBTDataMethod = blockClass.getMethod("getNbtData");
				hasNBTDataMethod = blockClass.getMethod("hasNbtData");
			}
			signClass = Class.forName("com.sk89q.worldedit.blocks.SignBlock");
			if (signClass != null) {
				getLinesMethod = signClass.getMethod("getText");
				signSetNBTDataMethod = signClass.getMethod("setNbtData", compoundTagClass);
				signConstructor = signClass.getConstructor(Integer.TYPE, Integer.TYPE);
			}
			if (blockClass != null) {
				getIdMethod = blockClass.getMethod("getId");
				getDataMethod = blockClass.getMethod("getData");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		classesValid = vectorClass != null && vectorConstructor != null && cuboidClipboardClass != null 
				&& getBlockMethod != null && blockClass != null && getSizeMethod != null
				&& getBlockXMethod != null && getBlockYMethod != null && getBlockZMethod != null
				&& getLinesMethod != null && signClass != null && compoundTagClass != null
				&& getNBTDataMethod != null && signSetNBTDataMethod != null && hasNBTDataMethod != null;
		
		return classesValid;
	}
	
	public Schematic(Object schematic) {
		weSchematic = schematic;
		
		// Center at the bottom X,Z center
		// This should be configurable, maybe?
		if (checkClasses()) {
			try {
				Object weSize = getSizeMethod.invoke(weSchematic);
				size = new Vector((Integer)getBlockXMethod.invoke(weSize), (Integer)getBlockYMethod.invoke(weSize), (Integer)getBlockZMethod.invoke(weSize)); 
				center = new Vector(Math.floor(size.getBlockX() / 2), 0, 
				Math.floor(size.getBlockZ() / 2));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} 
			
		if (center == null) {
			center = new Vector(0, 0, 0);
		}
		
		if (size == null) {
			size = new Vector(0, 0, 0);
		}
	}
	
	public boolean contains(Vector v) {
		int x = v.getBlockX() + center.getBlockX();
		int y = v.getBlockY() + center.getBlockY();
		int z = v.getBlockZ() + center.getBlockZ();
		
		return (x >= 0 && x <= size.getBlockX() && y >= 0 && y <= size.getBlockY() && z >= 0 && z <= size.getBlockZ());
	}
	
	@SuppressWarnings("deprecation")
	public MaterialAndData getBlock(Vector v) {
		if (!checkClasses()) return null;
		
		int x = v.getBlockX() + center.getBlockX();
		int y = v.getBlockY() + center.getBlockY();
		int z = v.getBlockZ() + center.getBlockZ();
		
		// TODO: Support Y-mirroring
		/*
		if (y < 0) {
			y = -y;
		}
		*/
		
		try {
			Object vector = vectorConstructor.newInstance(x, y, z);
			Object baseBlock = getBlockMethod.invoke(weSchematic, vector);
			Material material = Material.getMaterial((Integer)getIdMethod.invoke(baseBlock));
			int materialData = (int)(Integer)getDataMethod.invoke(baseBlock);
			MaterialAndData blockData = new MaterialAndData(material, (byte)materialData);
			
			// Note.. we don't actually get a SignBlock here, for some reason.
			// May have something to do with loading schematics not actually supporting sign
			// text, it doesn't work with //schematic and //paste, either.
			// It looks like //paste works in a dev build of WE, but it still doesn't give me the blocks
			// Looking at WE's code, it seems like the part that's needed is commented out... ??
			if (material == Material.SIGN_POST || material == Material.WALL_SIGN) {
				if ((Boolean)hasNBTDataMethod.invoke(baseBlock)) {
					Object signBlock = signConstructor.newInstance(material.getId(), materialData);
					Object nbtData = getNBTDataMethod.invoke(baseBlock);
					signSetNBTDataMethod.invoke(signBlock, nbtData);
					Bukkit.getLogger().info("Adding sign text");
					blockData.setSignLines((String[])getLinesMethod.invoke(signBlock));
				}
			}
			
			return blockData;
		} catch (ArrayIndexOutOfBoundsException ignore) {
		} catch (InvocationTargetException ignoreReferenced) {
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
}
