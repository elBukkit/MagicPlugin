package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;
import com.elmakers.mine.bukkit.utilities.borrowed.MaterialAndData;

public class MaterialBrush extends MaterialAndData {
	
	private enum BrushMode {
		MATERIAL,
		COPY,
		CLONE,
		REPLICATE
	};
	
	BrushMode mode = BrushMode.MATERIAL;
	Location cloneLocation = null;
	Location cloneTarget = null;
	Location materialTarget = null;
	String[] signLines = null;
	String commandLine = null;
	Inventory inventory = null;
	
	public MaterialBrush(final Material material, final  byte data) {
		super(material, data);
	}
	
	public void setMaterial(Material material) {
		this.material = material;
		this.data = 0;
		mode = BrushMode.MATERIAL;
		signLines = null;
		commandLine = null;
		inventory = null;
	}
	
	public void setMaterial(Material material, byte data) {
		setMaterial(material);
		this.data = data;
	}
	
	public void enableCloning() {
		this.mode = BrushMode.CLONE;
	}
	
	public void enableReplication() {
		this.mode = BrushMode.REPLICATE;
	}
	
	public void setData(byte data) {
		this.data = data;
	}
	
	public void setCloneLocation(Location cloneFrom) {
		cloneLocation = cloneFrom;
		materialTarget = cloneFrom;
		cloneTarget = null;
	}
	
	public void enableCopying() {
		mode = BrushMode.COPY;
	}
	
	public boolean isReady() {
		if ((mode == BrushMode.CLONE || mode == BrushMode.REPLICATE) && materialTarget != null) {
			Block block = materialTarget.getBlock();
			return (block.getChunk().isLoaded());
		}
		
		return true;
	}

	@SuppressWarnings("deprecation")
	public void setTarget(Location target) {
		if (mode == BrushMode.REPLICATE || mode == BrushMode.CLONE) {
			if (cloneTarget == null || mode == BrushMode.CLONE || 
				!target.getWorld().getName().equals(cloneTarget.getWorld().getName())) {
				cloneTarget = target;
			}
		}
		if (mode == BrushMode.COPY) {
			Block block = target.getBlock();
			if (block.getChunk().isLoaded()) {
				material = block.getType();
				data = block.getData();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public boolean update(Location target) {
		if (cloneLocation != null && (mode == BrushMode.CLONE || mode == BrushMode.REPLICATE)) {
			if (cloneTarget == null) cloneTarget = target;
			materialTarget = cloneLocation.clone();
			materialTarget.subtract(cloneTarget.toVector());
			materialTarget.add(target.toVector());
			
			Block block = materialTarget.getBlock();
			if (!block.getChunk().isLoaded()) return false;

			// Look for special block states
			signLines = null;
			commandLine = null;
			inventory = null;
			
			BlockState blockState = block.getState();
			if (blockState instanceof Sign) {
				Sign sign = (Sign)blockState;
				signLines = sign.getLines();
			} else if (blockState instanceof CommandBlock){
				CommandBlock command = (CommandBlock)blockState;
				commandLine = command.getCommand();
			} else if (blockState instanceof InventoryHolder) {
				InventoryHolder holder = (InventoryHolder)blockState;
				inventory = holder.getInventory();
			}
			
			material = block.getType();
			data = block.getData();
		}
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public void modify(Block block) {
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
		} else if (blockState instanceof InventoryHolder && inventory != null) {
			InventoryHolder holder = (InventoryHolder)blockState;
			Inventory newInventory = holder.getInventory();
			int maxSize = Math.min(newInventory.getSize(), inventory.getSize());
			for (int i = 0; i < maxSize; i++) {
				ItemStack item = inventory.getItem(i);
				item = InventoryUtils.getCopy(item);
				if (item != null) {
					newInventory.setItem(i, item);
				}
			}
		}
	}
	
	public void prepare() {
		if (cloneLocation != null) {
			Block block = cloneTarget.getBlock();
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load(true);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public boolean isDifferent(Block block) {
		Material blockMaterial = block.getType();
		byte blockData = block.getData();
		return blockMaterial != material || blockData != data;
	}
}
