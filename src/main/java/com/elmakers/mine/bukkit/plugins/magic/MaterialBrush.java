package com.elmakers.mine.bukkit.plugins.magic;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.blocks.MaterialAndData;
import com.elmakers.mine.bukkit.utilities.MaterialMapCanvas;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MaterialBrush extends MaterialAndData {
	
	private enum BrushMode {
		MATERIAL,
		COPY,
		CLONE,
		REPLICATE,
		MAP
	};
	
	private BrushMode mode = BrushMode.MATERIAL;
	private Location cloneLocation = null;
	private Location cloneTarget = null;
	private Location materialTarget = null;
	private final MagicController controller;
	private short mapId = -1;
	private MaterialMapCanvas mapCanvas = null;
	private Material mapMaterialBase = Material.STAINED_CLAY;
	
	public MaterialBrush(final MagicController controller, final Material material, final  byte data) {
		super(material, data);
		this.controller = controller;
	}
	
	@Override
	public void setMaterial(Material material, byte data) {
		if (!controller.isRestricted(material) && material.isBlock()) {
			super.setMaterial(material, data);
			mode = BrushMode.MATERIAL;
		}
	}
	
	public void enableCloning() {
		this.mode = BrushMode.CLONE;
	}
	
	public void enableErase() {
		this.setMaterial(Material.AIR);
	}
	
	public void enableMap() {
		this.mode = BrushMode.MAP;
		if (this.material == Material.WOOL || this.material == Material.STAINED_CLAY
			|| this.material == Material.STAINED_GLASS || this.material == Material.STAINED_GLASS_PANE
			|| this.material == Material.CARPET) {
			this.mapMaterialBase = this.material;
		}
	}
	
	public void enableReplication() {
		this.mode = BrushMode.REPLICATE;
	}
	
	public void setData(byte data) {
		this.data = data;
	}
	
	public void setMapId(short mapId) {
		this.mapCanvas = null;
		this.mapId = mapId;
	}
	
	public void setCloneLocation(Location cloneFrom) {
		cloneLocation = cloneFrom;
		materialTarget = cloneFrom;
		cloneTarget = null;
	}
	
	public void clearCloneTarget() {
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
		if (mode == BrushMode.REPLICATE || mode == BrushMode.CLONE || mode == BrushMode.MAP) {
			if (cloneTarget == null || mode == BrushMode.CLONE || 
				!target.getWorld().getName().equals(cloneTarget.getWorld().getName())) {
				cloneTarget = target;
			}
		}
		if (mode == BrushMode.COPY) {
			Block block = target.getBlock();
			updateFrom(block, controller.getRestrictedMaterials());
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
		if (cloneLocation == null || cloneTarget == null) return null;
		Location translated = target.clone();
		translated.setX(translated.getBlockX());
		translated.setY(translated.getBlockY());
		translated.setZ(translated.getBlockZ());
		Vector cloneVector = new Vector(cloneLocation.getBlockX(), cloneLocation.getBlockY(), cloneLocation.getBlockZ());
		translated.subtract(cloneVector);
		Vector cloneTargetVector = new Vector(cloneTarget.getBlockX(), cloneTarget.getBlockY(), cloneTarget.getBlockZ());
		translated.add(cloneTargetVector);
		translated.setWorld(targetWorld);
		return translated;
	}
	
	@SuppressWarnings("deprecation")
	public boolean update(Mage fromMage, Location target) {
		if (cloneLocation != null && (mode == BrushMode.CLONE || mode == BrushMode.REPLICATE)) {
			if (cloneTarget == null) cloneTarget = target;
			materialTarget = toTargetLocation(target);
			
			Block block = materialTarget.getBlock();
			if (!block.getChunk().isLoaded()) return false;

			updateFrom(block, controller.getRestrictedMaterials());
		}
		
		if (mode == BrushMode.MAP && mapId >= 0) {
			if (mapCanvas == null&& fromMage != null) {
				
				try {
					MapView mapView = Bukkit.getMap(mapId);
					if (mapView != null) {
						List<MapRenderer> renderers = mapView.getRenderers();
						if (renderers.size() > 0) {
							mapCanvas = new MaterialMapCanvas();
							MapRenderer renderer = renderers.get(0);
							// This is mainly here as a hack for my own urlmaps that do their own caching
							// Bukkit *seems* to want to do caching at the MapView level, but looking at the code-
							// they cache but never use the cache?
							// Anyway render gets called constantly so I'm not re-rendering on each render... but then
							// how to force a render to a canvas? So we re-initialize.
							renderer.initialize(mapView);
							renderer.render(mapView, mapCanvas, fromMage.getPlayer());
						}
					}
				} catch (Exception ex) {
					
				}
			}
			if (mapCanvas != null && cloneTarget != null) {
				Vector diff = target.toVector().subtract(cloneTarget.toVector());
				
				// TODO : Different orientations, centering, etc
				DyeColor mapColor = mapCanvas.getDyeColor(
						Math.abs(diff.getBlockX()) % MaterialMapCanvas.CANVAS_WIDTH, 
						Math.abs(diff.getBlockZ()) % MaterialMapCanvas.CANVAS_HEIGHT);
				if (mapColor != null) {
					updateTo(mapMaterialBase, mapColor.getData());
				} else {
					updateTo(Material.AIR, (byte)0);
				}
			}
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

	public void load(ConfigurationNode node)
	{
		try {
			cloneLocation = node.getLocation("clone_location");
			cloneTarget = node.getLocation("clone_target");
			materialTarget = node.getLocation("material_target");
			mapId = (short)node.getInt("map_id", mapId);
			material = node.getMaterial("material", material);
			data = (byte)node.getInt("data", data);
		} catch (Exception ex) {
			ex.printStackTrace();
			controller.getPlugin().getLogger().warning("Failed to load brush data: " + ex.getMessage());
		}
	}
	
	public void save(ConfigurationNode node)
	{
		try {
			if (cloneLocation != null) {
				node.setProperty("clone_location", cloneLocation);
			}
			if (cloneTarget != null) {
				node.setProperty("clone_target", cloneTarget);
			}
			if (materialTarget != null) {
				node.setProperty("material_target", materialTarget);
			}
			node.setProperty("map_id", (int)mapId);
			node.setProperty("material", material);
			node.setProperty("data", data);
		} catch (Exception ex) {
			ex.printStackTrace();
			controller.getLogger().warning("Failed to save brush data: " + ex.getMessage());
		}
	}
	
	public boolean isReplicating()
	{
		return mode == BrushMode.CLONE || mode == BrushMode.REPLICATE;
	}
}
