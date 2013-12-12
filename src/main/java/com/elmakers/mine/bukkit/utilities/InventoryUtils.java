package com.elmakers.mine.bukkit.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils extends NMSUtils
{
	protected static Object getNMSCopy(ItemStack stack) {
    	Object nms = null;
    	try {
			Method copyMethod = class_CraftItemStack.getMethod("asNMSCopy", stack.getClass());
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
                ex.printStackTrace();
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
			Method setStringMethod = class_NBTTagCompound.getMethod("setString", String.class, String.class);
			setStringMethod.invoke(node, tag, value);
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
			Object tagObject = getTag(craft);
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
			Object tagObject = getTag(craft);
			final Object enchList = class_NBTTagList.newInstance();
			Method setMethod = class_NBTTagCompound.getMethod("set", String.class, class_NBTBase);		
			setMethod.invoke(tagObject, "ench", enchList);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
    }

	public static Inventory createInventory(InventoryHolder holder, final int size, final String name) {
		Inventory inventory = null;
		try {
			Constructor<?> inventoryConstructor = class_CraftInventoryCustom.getConstructor(InventoryHolder.class, Integer.TYPE, String.class);
			inventory = (Inventory)inventoryConstructor.newInstance(holder, size, ChatColor.translateAlternateColorCodes('&', name));			
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return inventory;
	}

	public static boolean inventorySetItem(Inventory inventory, int index, ItemStack item) {
		try {
			Method setItemMethod = class_CraftInventoryCustom.getMethod("setItem", Integer.TYPE, ItemStack.class);
			setItemMethod.invoke(inventory, index, item);
			return true;
		} catch(Throwable ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public static boolean setInventoryResults(Inventory inventory, ItemStack item) {
		try {
			Method getResultsMethod = inventory.getClass().getMethod("getResultInventory");
			Object inv = getResultsMethod.invoke(inventory);
			Method setItemMethod = inv.getClass().getMethod("setItem", Integer.TYPE, class_ItemStack);
			setItemMethod.invoke(inv, 0, getHandle(item));
			return true;
		} catch(Throwable ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public static void addPotionEffect(LivingEntity entity, int color) {
		try {
			Method geHandleMethod = class_CraftLivingEntity.getMethod("getHandle");
			Object entityLiving = geHandleMethod.invoke(entity);
			Method getDataWatcherMethod = class_Entity.getMethod("getDataWatcher");
			Object dataWatcher = getDataWatcherMethod.invoke(entityLiving);
			Method watchMethod = class_DataWatcher.getMethod("watch", Integer.TYPE, Object.class);
			watchMethod.invoke(dataWatcher, (int)7, Integer.valueOf(color));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void removePotionEffect(LivingEntity entity) {
		addPotionEffect(entity, 0); // ?
	}
}