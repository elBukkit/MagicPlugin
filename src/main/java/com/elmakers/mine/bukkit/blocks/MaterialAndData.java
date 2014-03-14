package com.elmakers.mine.bukkit.blocks;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;

public class MaterialAndData {
	protected Material material;
	protected byte data;
	protected String[] signLines = null;
	protected String commandLine = null;
	protected String skullName = null;
	protected ItemStack[] inventoryContents = null;
	protected boolean isValid = true;

	public MaterialAndData() {
		material = Material.AIR;
		data = 0;
	}
	
	@SuppressWarnings("deprecation")
	public MaterialAndData(Block block) {
		material = block.getType();
		data = block.getData();
	}
	
	public MaterialAndData(MaterialAndData other) {
		updateFrom(other);
	}
	
	public void updateFrom(MaterialAndData other) {
		material = other.material;
		data = other.data;
		commandLine = other.commandLine;
		inventoryContents = other.inventoryContents;
		signLines = other.signLines;
		skullName = other.skullName;
		isValid = other.isValid;
	}
	
	public MaterialAndData(final Material material) {
		this.material = material;
		this.data = 0;
	}
	
	public MaterialAndData(final Material material, final  byte data) {
		this.material = material;
		this.data = data;
	}
	
	public void setMaterial(Material material, byte data) {
		this.material = material;
		this.data = data;
		signLines = null;
		commandLine = null;
		inventoryContents = null;
		skullName = null;
		isValid = true;
	}
	
	public void setMaterial(Material material) {
		setMaterial(material, (byte)0);
	}
	
	public void updateFrom(Block block) {
		updateFrom(block, null);
	}
	
	@SuppressWarnings("deprecation")
	public void updateFrom(Block block, Set<Material> restrictedMaterials) {
		if (!block.getChunk().isLoaded()) {
			block.getChunk().load(true);
			return;
		}

		Material blockMaterial = block.getType();
		if (restrictedMaterials != null && restrictedMaterials.contains(blockMaterial)) {
			isValid = false;
			return;
		}
		// Look for special block states
		signLines = null;
		commandLine = null;
		inventoryContents = null;
		skullName = null;
		
		BlockState blockState = block.getState();
		if (blockState instanceof Sign) {
			Sign sign = (Sign)blockState;
			signLines = sign.getLines();
		} else if (blockState instanceof CommandBlock){
			CommandBlock command = (CommandBlock)blockState;
			commandLine = command.getCommand();
		} else if (blockState instanceof InventoryHolder) {
			InventoryHolder holder = (InventoryHolder)blockState;
			Inventory holderInventory = holder.getInventory();
			inventoryContents = holderInventory.getContents();
		} else if (blockState instanceof Skull) {
			Skull skull = (Skull)blockState;
			skullName = skull.getOwner();
		}
		
		material = blockMaterial;
		data = block.getData();
		isValid = true;
	}
	
	@SuppressWarnings("deprecation")
	public void modify(Block block) {
		if (!isValid) return;
		
		try {
			// Clear chests so they don't dump their contents.
			BlockState oldState = block.getState();
			if (oldState instanceof InventoryHolder) {
				InventoryHolder holder = (InventoryHolder)oldState;
				Inventory inventory = holder.getInventory();
				inventory.clear();
			}
			
			block.setType(material);
			block.setData(data);
			BlockState blockState = block.getState();
			if (blockState instanceof Sign && signLines != null) {
				Sign sign = (Sign)blockState;
				for (int i = 0; i < signLines.length; i++) {
					sign.setLine(i, signLines[i]);
				}
				sign.update();
			} else if (blockState instanceof CommandBlock && commandLine != null) {
				CommandBlock command = (CommandBlock)blockState;
				command.setCommand(commandLine);
				command.update();
			} else if (blockState instanceof InventoryHolder && inventoryContents != null) {
				InventoryHolder holder = (InventoryHolder)blockState;
				Inventory newInventory = holder.getInventory();
				int maxSize = Math.min(newInventory.getSize(), inventoryContents.length);
				for (int i = 0; i < maxSize; i++) {
					ItemStack item = inventoryContents[i];
					item = InventoryUtils.getCopy(item);
					if (item != null) {
						newInventory.setItem(i, item);
					}
				}
			} else if (blockState instanceof Skull && skullName != null) {
				Skull skull = (Skull)blockState;
				skull.setOwner(skullName);
				skull.update();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public byte getData() {
		return data;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public String getKey() {
		String materialKey = material.name().toLowerCase();
		if (data != 0) {
			materialKey += ":" + data;
		}
		
		return materialKey;
	}

	@SuppressWarnings("deprecation")
	public boolean isDifferent(Block block) {
		Material blockMaterial = block.getType();
		byte blockData = block.getData();
		if (blockMaterial != material || blockData != data) {
			return true;
		}
		
		// Special cases
		BlockState blockState = block.getState();
		if (blockState instanceof Sign && signLines != null) {
			Sign sign = (Sign)blockState;
			String[] currentLines = sign.getLines();
			for (int i = 0; i < signLines.length; i++) {
				if (!currentLines[i].equals(signLines[i])) {
					return true;
				}
			}
		} else if (blockState instanceof CommandBlock && commandLine != null) {
			CommandBlock command = (CommandBlock)blockState;
			if (!command.getCommand().equals(commandLine)) {
				return true;
			}
		} else if (blockState instanceof InventoryHolder && inventoryContents != null) {
			// Just copy it over.... not going to compare inventories :P
			return true;
		}
		
		return false;
	}
	
	public void setSignLines(String[] lines) {
		signLines = lines.clone();
	}
	
	public void setInventoryContents(ItemStack[] contents) {
		inventoryContents = contents;
	}
	
	public void setCommandLine(String command) {
		commandLine = command;
	}

	@SuppressWarnings("deprecation")
	public ItemStack getItemStack(int amount)
	{
		return new ItemStack(material, amount, (short)0, data);
	}
}
