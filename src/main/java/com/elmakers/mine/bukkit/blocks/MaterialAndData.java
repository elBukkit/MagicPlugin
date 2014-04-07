package com.elmakers.mine.bukkit.blocks;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
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
	protected String customName = null;
	protected ItemStack[] inventoryContents = null;
	protected boolean isValid = true;

	public MaterialAndData() {
		material = Material.AIR;
		data = 0;
	}
	
	public MaterialAndData(Block block) {
		updateFrom(block);
	}
	
	public MaterialAndData(MaterialAndData other) {
		updateFrom(other);
	}
	
	public MaterialAndData(final Material material, final  byte data, final String customName) {
		this(material, data);
		this.customName = customName;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public int hashCode() {
		// Note that this does not incorporate any metadata!
		return (material.getId() << 8) | data;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (!(obj instanceof MaterialAndData)) {
	        return false;
	    }
	    
	    MaterialAndData other = (MaterialAndData)obj;
        return other.data == data && other.material == material;
	}
	
	public void updateFrom(MaterialAndData other) {
		material = other.material;
		data = other.data;
		commandLine = other.commandLine;
		inventoryContents = other.inventoryContents;
		signLines = other.signLines;
		customName = other.customName;
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
		customName = null;
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
		customName = null;
		
		try {
			BlockState blockState = block.getState();
			if (blockState instanceof Sign) {
				Sign sign = (Sign)blockState;
				signLines = sign.getLines();
			} else if (blockState instanceof CommandBlock){
				// This seems to occasionally throw exceptions...
				CommandBlock command = (CommandBlock)blockState;
				commandLine = command.getCommand();
				customName = command.getName();
			} else if (blockState instanceof InventoryHolder) {
				InventoryHolder holder = (InventoryHolder)blockState;
				Inventory holderInventory = holder.getInventory();
				inventoryContents = holderInventory.getContents();
			} else if (blockState instanceof Skull) {
				Skull skull = (Skull)blockState;
				customName = skull.getOwner();
			} else if (blockState instanceof CreatureSpawner) {
				CreatureSpawner spawner = (CreatureSpawner)blockState;
				customName = spawner.getCreatureTypeName();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
				if (customName != null) {
					command.setName(customName);
				}
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
			} else if (blockState instanceof Skull && customName != null) {
				Skull skull = (Skull)blockState;
				skull.setOwner(customName);
				skull.update();
			} else if (blockState instanceof CreatureSpawner && customName != null && customName.length() > 0) {
				CreatureSpawner spawner = (CreatureSpawner)blockState;
				spawner.setCreatureTypeByName(customName);
				spawner.update();
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
		
		// Some special keys
		if (material == Material.SKULL && data == 3 && customName != null && customName.length() > 0) {
			materialKey += ":" + customName;
		}
		else if (material == Material.MOB_SPAWNER && customName != null && customName.length() > 0) {
			materialKey += ":" + customName;
		}
		else if (data != 0) {
			materialKey += ":" + data;
		}
		
		return materialKey;
	}
	
	// TODO: Should this just be !isDifferent .. ? It's fast right now.
	@SuppressWarnings("deprecation")
	public boolean is(Block block) {
		return material == block.getType() && data == block.getData();
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
	
	public void setCustomName(String customName) {
		this.customName = customName;
	}
	
	public String getCustomName() {
		return customName;
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
