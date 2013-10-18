package com.elmakers.mine.bukkit.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils 
{
	private static String versionPrefix = "";

	private static Class<?> class_ItemStack;
	private static Class<?> class_NBTBase;
	private static Class<?> class_NBTTagCompound;
	private static Class<?> class_NBTTagList;
	private static Class<?> class_CraftInventoryCustom;
	private static Class<?> class_CraftItemStack;

	static 
	{
		// Find classes Bukkit hides from us. :-D
		// Much thanks to @DPOHVAR for sharing the PowerNBT code that powers the reflection approach.
		try { 
			String className = Bukkit.getServer().getClass().getName();
			String[] packages = className.split("\\.");
			if (packages.length == 5) {
				versionPrefix = packages[3] + ".";
			}

			class_ItemStack = fixBukkitClass("net.minecraft.server.ItemStack");
			class_NBTBase = fixBukkitClass("net.minecraft.server.NBTBase");
			class_NBTTagCompound = fixBukkitClass("net.minecraft.server.NBTTagCompound");
			class_NBTTagList = fixBukkitClass("net.minecraft.server.NBTTagList");
			class_CraftInventoryCustom = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftInventoryCustom");
			class_CraftItemStack = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftItemStack");
		} 
		catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	private static Class<?> fixBukkitClass(String className) {
		className = className.replace("org.bukkit.craftbukkit.", "org.bukkit.craftbukkit." + versionPrefix);
		className = className.replace("net.minecraft.server.", "net.minecraft.server." + versionPrefix);
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected static Object getNMSCopy(ItemStack stack) {
		Object nms = null;
		try {
			Method copyMethod = class_CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
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

	public static String getMeta(ItemStack stack, String tag) {
		if (stack == null) return null;
		String meta = null;
		try {
			Object craft = getNMSCopy(stack);
			Object tagObject = getTag(craft);
			Method getStringMethod = class_NBTTagCompound.getMethod("getString", String.class);
			meta = (String)getStringMethod.invoke(tagObject, tag);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return meta;
	}

	public static ItemStack setMeta(ItemStack stack, String tag, String value) {
		if (stack == null) return null;
		try {
			Object craft = getNMSCopy(stack);
			Object tagObject = getTag(craft);
			Method setStringMethod = class_NBTTagCompound.getMethod("setString", String.class, String.class);
			setStringMethod.invoke(tagObject, tag, value);
			Method mirrorMethod = class_CraftItemStack.getMethod("asCraftMirror", craft.getClass());
			stack = (ItemStack)mirrorMethod.invoke(null, craft);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}

		return stack;
	}

	public static String inventoryToString(final Inventory inventory) {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final DataOutputStream dataOutput = new DataOutputStream(outputStream);
		try {        
			final Object itemList = class_NBTTagList.newInstance();
			for (int i = 0; i < inventory.getSize(); i++) {
				final Object outputObject = class_NBTTagCompound.newInstance();
				Object craft = null;
				final ItemStack is = inventory.getItem(i);
				if (is != null) {
					craft = getNMSCopy(is);
				} else {
					craft = null;
				}
				if (craft != null && class_ItemStack.isInstance(craft)) {
					Method saveMethod = class_ItemStack.getMethod("save", outputObject.getClass());
					saveMethod.invoke(craft, outputObject);
				}
				Method addMethod = class_NBTTagList.getMethod("add", class_NBTBase);
				addMethod.invoke(itemList, outputObject);
			}

			// This bit is kind of ugly and prone to break between versions
			// Well, moreso than the rest of this, even.
			Method saveMethod = class_NBTBase.getMethod("a", class_NBTBase, DataOutput.class);
			saveMethod.invoke(null, itemList, dataOutput);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}

		return new BigInteger(1, outputStream.toByteArray()).toString(32);
	}

	public static Inventory stringToInventory(final String data, final String name) {
		Inventory inventory = null;

		try {
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

			// More MC internals :(
			Method loadMethod = class_NBTBase.getMethod("a", DataInput.class);
			final Object itemList = loadMethod.invoke(null, new DataInputStream(inputStream));

			Method sizeMethod = class_NBTTagList.getMethod("size");
			Method getMethod = class_NBTTagList.getMethod("get", Integer.TYPE);
			final int listSize = (Integer)sizeMethod.invoke(itemList);

			Method isEmptyMethod = class_NBTTagCompound.getMethod("isEmpty");			
			Method setItemMethod = class_CraftInventoryCustom.getMethod("setItem", Integer.TYPE, ItemStack.class);

			inventory = createInventory(null, listSize, name);

			for (int i = 0; i < listSize; i++) {
				final Object inputObject = getMethod.invoke(itemList, i);
				if (!(Boolean)isEmptyMethod.invoke(inputObject)) {
					Method createMethod = class_ItemStack.getMethod("createStack", inputObject.getClass());
					Object newStack = createMethod.invoke(null, inputObject);
					Method bukkitCopyMethod = class_CraftItemStack.getMethod("asBukkitCopy", class_ItemStack);
					Object newCraftStack = bukkitCopyMethod.invoke(null, newStack);
					setItemMethod.invoke(inventory, i, newCraftStack);
				}
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return inventory;
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
}