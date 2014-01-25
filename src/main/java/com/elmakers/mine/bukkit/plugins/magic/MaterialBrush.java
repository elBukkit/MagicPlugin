package com.elmakers.mine.bukkit.plugins.magic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.utilities.borrowed.MaterialAndData;

public class MaterialBrush extends MaterialAndData {
	
	private enum BrushMode {
		MATERIAL,
		COPY,
		CLONE,
		REPLICATE
	};
	
	
	
	private BrushMode mode = BrushMode.MATERIAL;
	private Location cloneLocation = null;
	private Location cloneTarget = null;
	private Location materialTarget = null;
	private final MagicController controller;
	
	public MaterialBrush(final MagicController controller, final Material material, final  byte data) {
		super(material, data);
		this.controller = controller;
	}
	
	@Override
	public void setMaterial(Material material, byte data) {
		if (controller.isBuildable(material)) {
			super.setMaterial(material, data);
		}
		mode = BrushMode.MATERIAL;
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
	
	public boolean hasCloneTarget() {
		return cloneLocation != null && cloneTarget != null;
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

	public void setTarget(Location target) {
		if (mode == BrushMode.REPLICATE || mode == BrushMode.CLONE) {
			if (cloneTarget == null || mode == BrushMode.CLONE || 
				!target.getWorld().getName().equals(cloneTarget.getWorld().getName())) {
				cloneTarget = target;
			}
		}
		if (mode == BrushMode.COPY) {
			Block block = target.getBlock();
			updateFrom(block, controller.getBuildingMaterials());
		}
	}
	
	public Location toTargetLocation(Location target) {
		if (cloneLocation == null || cloneTarget == null) return null;
		Location translated = cloneLocation.clone();
		translated.subtract(cloneTarget.toVector());
		translated.add(target.toVector());
		return translated;
	}
	
	public Location fromTargetLocation(World targetWorld, Location target) {
		
		/// TODO FIX THIS!
		if (cloneLocation == null || cloneTarget == null) return null;
		Location translated = target.clone();
		translated.subtract(cloneLocation.toVector());
		translated.add(cloneTarget.toVector());
		translated.setWorld(targetWorld);
		return translated;
	}
	
	public boolean update(Location target) {
		if (cloneLocation != null && (mode == BrushMode.CLONE || mode == BrushMode.REPLICATE)) {
			if (cloneTarget == null) cloneTarget = target;
			materialTarget = toTargetLocation(target);
			
			Block block = materialTarget.getBlock();
			if (!block.getChunk().isLoaded()) return false;

			updateFrom(block, controller.getBuildingMaterials());
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
