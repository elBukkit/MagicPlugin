package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

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
	
	public MaterialBrush(final Material material, final  byte data) {
		super(material, data);
	}
	
	public void setMaterial(Material material) {
		this.material = material;
		this.data = 0;
		mode = BrushMode.MATERIAL;
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
