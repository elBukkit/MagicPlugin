package com.elmakers.mine.bukkit.blocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.util.Vector;

public class Schematic {
	private static Class<?> vectorClass;
	private static Class<?> cuboidClipboardClass;
	private static Class<?> blockClass;
	private static Constructor<?> vectorConstructor;
	private static Method getBlockMethod;
	private static Method getIdMethod;
	private static Method getDataMethod;
	private static Method getSizeMethod;
	private static Method getBlockXMethod;
	private static Method getBlockYMethod;
	private static Method getBlockZMethod;
	
	private final Object weSchematic;
	private Vector center;
	private Vector size;
	
	private static boolean checkClasses() {
		try {
			if (vectorClass == null) vectorClass = Class.forName("com.sk89q.worldedit.Vector");
			if (vectorClass != null) {
				if (vectorConstructor == null) vectorConstructor = vectorClass.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
				if (getBlockXMethod == null) getBlockXMethod = vectorClass.getMethod("getBlockX");				
				if (getBlockYMethod == null) getBlockYMethod = vectorClass.getMethod("getBlockY");				
				if (getBlockZMethod == null) getBlockZMethod = vectorClass.getMethod("getBlockZ");
			}
			if (cuboidClipboardClass == null) cuboidClipboardClass = Class.forName("com.sk89q.worldedit.CuboidClipboard");
			if (cuboidClipboardClass != null) {
				if (getBlockMethod == null) getBlockMethod = cuboidClipboardClass.getMethod("getBlock", vectorClass);
				if (getSizeMethod == null) getSizeMethod = cuboidClipboardClass.getMethod("getSize");				
			}
			if (blockClass == null) {
				blockClass = Class.forName("com.sk89q.worldedit.foundation.Block");
			}
			if (blockClass != null) {
				if (getIdMethod == null) getIdMethod = blockClass.getMethod("getId");
				if (getDataMethod == null) getDataMethod = blockClass.getMethod("getData");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return vectorClass != null && vectorConstructor != null && cuboidClipboardClass != null 
				&& getBlockMethod != null && blockClass != null && getSizeMethod != null
				&& getBlockXMethod != null && getBlockYMethod != null && getBlockZMethod != null;
	}
	
	public Schematic(Object schematic) {
		weSchematic = schematic;
		
		// Center at the bottom X,Z center
		// This should be configurable, maybe?
		if (checkClasses()) {
			try {
				Object weSize = getSizeMethod.invoke(weSchematic);
				size = new Vector((Integer)getBlockXMethod.invoke(weSize) / 2, (Integer)getBlockYMethod.invoke(weSize) / 2, (Integer)getBlockZMethod.invoke(weSize) / 2); 
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
		
		return (x >= 0 && x < size.getBlockX() && y >= 0 && y < size.getBlockY() && z >= 0 && z < size.getBlockZ());
	}
	
	@SuppressWarnings("deprecation")
	public MaterialAndData getBlock(Vector v) {
		if (!checkClasses()) return null;
		
		int x = v.getBlockX() + center.getBlockX();
		int y = v.getBlockY() + center.getBlockY();
		int z = v.getBlockZ() + center.getBlockZ();
		
		try {
			Object vector = vectorConstructor.newInstance(x, y, z);
			Object baseBlock = getBlockMethod.invoke(weSchematic, vector);
			return new MaterialAndData(Material.getMaterial((Integer)getIdMethod.invoke(baseBlock)), (byte)(int)(Integer)getDataMethod.invoke(baseBlock));
		} catch (ArrayIndexOutOfBoundsException ignore) {
		} catch (InvocationTargetException ignoreReferenced) {
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
}
