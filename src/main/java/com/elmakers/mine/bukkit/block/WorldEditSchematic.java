package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.Schematic;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.ChestBlock;
import com.sk89q.worldedit.blocks.SignBlock;

public class WorldEditSchematic implements Schematic{
	
	private final CuboidClipboard weSchematic;
	private Vector center;
	private Vector size;
	
	public WorldEditSchematic(Object schematic) {
		weSchematic = (CuboidClipboard)schematic;
		
		// Center at the bottom X,Z center
		// This should be configurable, maybe?
		try {
			com.sk89q.worldedit.Vector weSize = weSchematic.getSize();
			size = new Vector(weSize.getBlockX(), weSize.getBlockY(), weSize.getBlockZ()); 
			center = new Vector(Math.floor(size.getBlockX() / 2), 0, Math.floor(size.getBlockZ() / 2));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
			
		if (center == null) {
			center = new Vector(0, 0, 0);
		}
		
		if (size == null) {
			size = new Vector(0, 0, 0);
		}
	}
	
	@Override
	public boolean contains(Vector v) {
		int x = v.getBlockX() + center.getBlockX();
		int y = v.getBlockY() + center.getBlockY();
		int z = v.getBlockZ() + center.getBlockZ();
		
		return (x >= 0 && x <= size.getBlockX() && y >= 0 && y <= size.getBlockY() && z >= 0 && z <= size.getBlockZ());
	}
	
	@SuppressWarnings({ "deprecation" })
	@Override
	public MaterialAndData getBlock(Vector v) {
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
			com.sk89q.worldedit.Vector vector = new com.sk89q.worldedit.Vector(x, y, z);
			BaseBlock baseBlock = weSchematic.getBlock(vector);
			Material material = Material.getMaterial(baseBlock.getId());
			int materialData = baseBlock.getData();
			MaterialAndData blockData = new MaterialAndData(material, (byte)materialData);
			
			// Note.. we don't actually get a SignBlock here, for some reason.
			// May have something to do with loading schematics not actually supporting sign
			// text, it doesn't work with //schematic and //paste, either.
			// It looks like //paste works in a dev build of WE, but it still doesn't give me the blocks
			// Looking at WE's code, it seems like the part that's needed is commented out... ??
			if (material == Material.SIGN_POST || material == Material.WALL_SIGN) {
				try {
					if (baseBlock.hasNbtData()) {
						SignBlock signBlock = new SignBlock(material.getId(), materialData);
						CompoundTag nbtData = baseBlock.getNbtData();
						signBlock.setNbtData(nbtData);
						blockData.setSignLines(signBlock.getText());
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
			} else if (material == Material.COMMAND) {
				try {
					if (baseBlock.hasNbtData()) {
						CompoundTag nbtRoot = baseBlock.getNbtData();
						Map<String, Tag> rootValues = nbtRoot.getValue();
						if (rootValues.containsKey("Command")) {
							Object commandValue = rootValues.get("Command").getValue();
							blockData.setCommandLine((String)commandValue);
						}
						if (rootValues.containsKey("CustomName")) {
							Object nameValue = rootValues.get("CustomName").getValue();
							blockData.setCustomName((String)nameValue);
						}
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
			} else if (material == Material.CHEST) {
				try {
					if (baseBlock.hasNbtData()) {
						ChestBlock chestBlock = new ChestBlock(materialData);
						CompoundTag nbtRoot = baseBlock.getNbtData();
						chestBlock.setNbtData(nbtRoot);
						BaseItemStack[] items = chestBlock.getItems();
						
						if (items != null && items.length > 0) {
							ItemStack[] contents = new ItemStack[items.length];
							for (int i = 0; i < items.length; i++) {
								if (items[i] != null) {
									Material itemMaterial = Material.getMaterial(items[i].getType());
									
									// Bukkit.getLogger().info("Item from chest: " + itemMaterial + " at " + i + " / " + contents.length);
									
									short itemData = items[i].getData();
									int itemAmount = items[i].getAmount();		
									ItemStack newStack = new ItemStack(itemMaterial, itemAmount,itemData);
									
									Map<Integer, Integer> enchantments = items[i].getEnchantments();
									if (enchantments != null && enchantments.size() > 0) {
										for (Entry<Integer, Integer> enchantment : enchantments.entrySet()) {
											try {
												Enchantment enchantmentType = Enchantment.getById(enchantment.getKey());
												newStack.addEnchantment(enchantmentType, enchantment.getValue());
											} catch (Exception ex) {
												// This seems to happen a lot .. like on potions especially.
												ex.printStackTrace();
											}
										}
									}
									contents[i] = newStack;
								}
							}
							blockData.setInventoryContents(contents);
						}
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
			}
			
			return blockData;
		} catch (ArrayIndexOutOfBoundsException ignore) {
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Collection<com.elmakers.mine.bukkit.api.entity.EntityData> getAllEntities()
	{
		List<com.elmakers.mine.bukkit.api.entity.EntityData> entities = new ArrayList<com.elmakers.mine.bukkit.api.entity.EntityData>();
		return entities;
	}
	
	@Override
	public Collection<com.elmakers.mine.bukkit.api.entity.EntityData> getEntities(Location center, int radius)
	{
		/*
		if (weSchematic == null) return entities;
		// No accessor! :(
		try {
			Field entitiesField = weSchematic.getClass().getDeclaredField("entities");
			entitiesField.setAccessible(true);
			
			@SuppressWarnings("unchecked")
			// List<Object> schematicEntities = (List<Object>)entitiesField.get(weSchematic);
			
			// Bukkit.getLogger().info("Found " + schematicEntities.size() + " entities");
			

		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		*/
		return getAllEntities();
	}
}