package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.utilities.borrowed.MaterialAndData;

public class MaterialBrush extends MaterialAndData {
	
	Location cloneLocation = null;
	Location cloneTarget = null;
	
	boolean targetLocked = false;
	boolean copyEnabled = false;
	
	public MaterialBrush(final Material material, final  byte data) {
		super(material, data);
	}
	
	protected void reset() {
		targetLocked = false;
		copyEnabled = false;
		cloneLocation = null;
		cloneTarget = null;
	}
	
	public void setMaterial(Material material) {
		this.material = material;
		this.data = 0;
		reset();
	}
	
	public void setMaterial(Material material, byte data) {
		setMaterial(material);
		this.data = data;
	}
	
	public void setData(byte data) {
		this.data = data;
	}
	
	public void enableCloning(Location cloneFrom) {
		reset();
		cloneLocation = cloneFrom;
	}
	
	public void lockTarget() {
		targetLocked = true;
	}
	
	public void enableCopying(boolean enable) {
		reset();
		copyEnabled = enable;
	}
	
	public void enableCopying() {
		enableCopying(true);
	}
	
	public boolean isReady() {
		if (cloneLocation != null) {
			Block block = cloneLocation.getBlock();
			return (block.getChunk().isLoaded());
		}
		
		return true;
	}

	@SuppressWarnings("deprecation")
	public void setTarget(Location target) {
		if (cloneTarget == null || !targetLocked || !target.getWorld().getName().equals(cloneTarget.getWorld().getName())) {
			cloneTarget = target;
		}
		if (copyEnabled) {
			Block block = target.getBlock();
			if (block.getChunk().isLoaded()) {
				material = block.getType();
				data = block.getData();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public boolean update(Location target) {
		if (cloneLocation != null) {
			Location materialTarget = cloneLocation.clone();
			materialTarget.subtract(cloneTarget.toVector());
			materialTarget.add(target.toVector());
			
			Block block = materialTarget.getBlock();
			if (!block.getChunk().isLoaded()) return false;
			
			material = block.getType();
			data = block.getData();
		}
		
		return true;
	}
	
	public void prepare() {
		if (cloneLocation != null) {
			Block block = cloneTarget.getBlock();
			if (!block.getChunk().isLoaded()) {
				block.getChunk().load(true);
			}
		}
	}
}
