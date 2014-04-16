package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.utilities.MaterialMapCanvas;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MaterialBrush extends MaterialAndData {
	
	private enum BrushMode {
		MATERIAL,
		ERASE,
		COPY,
		CLONE,
		REPLICATE,
		MAP,
		SCHEMATIC
	};

	public static final String ERASE_MATERIAL_KEY = "erase";
	public static final String COPY_MATERIAL_KEY = "copy";
	public static final String CLONE_MATERIAL_KEY = "clone";
	public static final String REPLICATE_MATERIAL_KEY = "replicate";
	public static final String MAP_MATERIAL_KEY = "map";
	public static final String SCHEMATIC_MATERIAL_KEY = "schematic";
	
	// This does not include schematics
	public static final String[] SPECIAL_MATERIAL_KEYS = {ERASE_MATERIAL_KEY, COPY_MATERIAL_KEY,
		CLONE_MATERIAL_KEY, REPLICATE_MATERIAL_KEY, MAP_MATERIAL_KEY};
	
	public static Material EraseMaterial = Material.SULPHUR;
	public static Material CopyMaterial = Material.SUGAR;
	public static Material CloneMaterial = Material.NETHER_STALK;
	public static Material ReplicateMaterial = Material.PUMPKIN_SEEDS;
	public static Material MapMaterial = Material.MAP;
	public static Material SchematicMaterial = Material.PAPER;
	
	public static boolean SchematicsEnabled = false;	
	
	public static final Material DEFAULT_MATERIAL = Material.DIRT;
	
	private BrushMode mode = BrushMode.MATERIAL;
	private Location cloneLocation = null;
	private Location cloneTarget = null;
	private Location materialTarget = null;
	private Vector targetOffset = null;
	private World targetWorld = null;
	private final Mage mage;
	private short mapId = -1;
	private MaterialMapCanvas mapCanvas = null;
	private Material mapMaterialBase = Material.STAINED_CLAY;
	private Schematic schematic;
	private boolean fillWithAir = true;
	private Vector orientVector = null;
	
	public MaterialBrush(final Mage mage, final Material material, final  byte data) {
		super(material, data);
		this.mage = mage;
	}
	
	public MaterialBrush(final Mage mage, final Location location, final String materialKey) {
		super(DEFAULT_MATERIAL, (byte)0);
		this.mage = mage;
		update(materialKey);
		activate(location, materialKey);
	}
	
	public static String getMaterialKey(Material material) {
		return getMaterialKey(material, true);
	}
	
	public static String getMaterialKey(Material material, boolean allowItems) {
		String materialKey = null;
		if (material == null) return null;

		if (material == EraseMaterial) {
			materialKey = ERASE_MATERIAL_KEY;
		} else if (material == CopyMaterial) {
			materialKey = COPY_MATERIAL_KEY;
		} else if (material == CloneMaterial) {
			materialKey = CLONE_MATERIAL_KEY;
		} else if (material == MapMaterial) {
			materialKey = MAP_MATERIAL_KEY;
		} else if (material == ReplicateMaterial) {
			materialKey = REPLICATE_MATERIAL_KEY;
		} else if (SchematicsEnabled && material == SchematicMaterial) {
			// This would be kinda broken.. might want to revisit all this.
			// This method is only called by addMaterial at this point,
			// which should only be called with real materials anyway.
			materialKey = SCHEMATIC_MATERIAL_KEY;
		} else if (allowItems || material.isBlock()) {
			materialKey = material.name().toLowerCase();
		}
		
		return materialKey;
	}
	
	public static String getMaterialKey(Material material, byte data) {
		return getMaterialKey(material, data, true);
	}

	public static String getMaterialKey(Material material, byte data, boolean allowItems) {
		String materialKey = MaterialBrush.getMaterialKey(material, allowItems);
		if (materialKey == null) {
			return null;
		}
		if (data != 0) {
			materialKey += ":" + data;
		}
		
		return materialKey;
	}
	
	public static String getMaterialKey(MaterialAndData materialData) {
		return getMaterialKey(materialData.getMaterial(), materialData.getData(), true);
	}
	
	public static String getMaterialKey(MaterialAndData materialData, boolean allowItems) {
		return getMaterialKey(materialData.getMaterial(), materialData.getData(), allowItems);
	}
	
	public static boolean isSpecialMaterialKey(String materialKey) {
		if (materialKey == null || materialKey.length() == 0) return false;
		materialKey = splitMaterialKey(materialKey)[0];
		return COPY_MATERIAL_KEY.equals(materialKey) || ERASE_MATERIAL_KEY.equals(materialKey) || 
			   REPLICATE_MATERIAL_KEY.equals(materialKey) || CLONE_MATERIAL_KEY.equals(materialKey) || 
			   MAP_MATERIAL_KEY.equals(materialKey) || (SchematicsEnabled && SCHEMATIC_MATERIAL_KEY.equals(materialKey));
	}
	
	public static String getMaterialName(MaterialAndData material) {
		return getMaterialName(getMaterialKey(material));
	}

	public static String getMaterialName(Material material, byte data) {
		return getMaterialName(getMaterialKey(material, data));
	}
	
	public static String getMaterialName(String materialKey) {
		if (materialKey == null) return null;
		String materialName = materialKey;
		String[] namePieces = splitMaterialKey(materialName);
		if (namePieces.length == 0) return null;
		
		materialName = namePieces[0];
		
		if (!MaterialBrush.isSpecialMaterialKey(materialKey)) {
			return MaterialAndData.getMaterialName(materialKey);
		} else if (materialName.startsWith(MaterialBrush.SCHEMATIC_MATERIAL_KEY) && namePieces.length > 1) {
			materialName = namePieces[1];
		}
		
		materialName = materialName.toLowerCase().replace('_', ' ');
		
		return materialName;
	}
	
	public String getName() {
		String brushKey = getKey();
		switch (mode) {
		case CLONE: brushKey = CLONE_MATERIAL_KEY; break;
		case REPLICATE: brushKey = REPLICATE_MATERIAL_KEY; break;
		case COPY: brushKey = COPY_MATERIAL_KEY; break;
		case MAP: brushKey = MAP_MATERIAL_KEY; break;
		case SCHEMATIC: brushKey = SCHEMATIC_MATERIAL_KEY + ":" + customName; break;
			default: break;
		}
		return getMaterialName(brushKey);
	}
	
	public static MaterialAndData parseMaterialKey(String materialKey) {
		return parseMaterialKey(materialKey, true);
	}
	
	public static MaterialAndData parseMaterialKey(String materialKey, boolean allowItems) {
		if (materialKey == null || materialKey.length() == 0) return null;
		
		Material material = DEFAULT_MATERIAL;
		byte data = 0;
		String customName = "";
		String[] pieces = splitMaterialKey(materialKey);
				
		if (materialKey.equals(ERASE_MATERIAL_KEY)) {
			material = EraseMaterial;
		} else if (materialKey.equals(COPY_MATERIAL_KEY)) {
			material = CopyMaterial;
		} else if (materialKey.equals(CLONE_MATERIAL_KEY)) {
			material = CloneMaterial;
		} else if (materialKey.equals(REPLICATE_MATERIAL_KEY)) {
			material = ReplicateMaterial;
		} else if (materialKey.equals(MAP_MATERIAL_KEY)) {
			material = MapMaterial;
		} else if (SchematicsEnabled && pieces[0].equals(SCHEMATIC_MATERIAL_KEY) && pieces.length > 1) {
			material = SchematicMaterial;
			customName = pieces[1];
		} else {
			MaterialAndData basic = new MaterialAndData(materialKey);
			
			// Prevent building with items
			if (!allowItems && !basic.getMaterial().isBlock()) {
				return null;
			}
			
			return basic;
		}
				
		return new MaterialAndData(material, data, customName);
	}
	
	public static boolean isValidMaterial(String materialKey) {
		return parseMaterialKey(materialKey) != null;
	}
	
	public static boolean isValidMaterial(String materialKey, boolean allowItems) {
		return parseMaterialKey(materialKey, allowItems) != null;
	}

	public void update(String activeMaterial) {
		String pieces[] = splitMaterialKey(activeMaterial);
		if (activeMaterial.equals(COPY_MATERIAL_KEY)) {
			enableCopying();
		} else if (activeMaterial.equals(CLONE_MATERIAL_KEY)) {
			enableCloning();
		} else if (activeMaterial.equals(REPLICATE_MATERIAL_KEY)) {
			enableReplication();
		} else if (activeMaterial.equals(MAP_MATERIAL_KEY)) {
			enableMap();
		} else if (activeMaterial.equals(ERASE_MATERIAL_KEY)) {
			enableErase();
		} else if (pieces.length > 1 && pieces[0].equals(SCHEMATIC_MATERIAL_KEY)) {
			enableSchematic(pieces[1]);
		} else {
			MaterialAndData material = parseMaterialKey(activeMaterial);
			if (material != null) {
				setMaterial(material.getMaterial(), material.getData());
			}
		}
	}
	
	public void activate(final Location location, final String material) {
		String materialKey = splitMaterialKey(material)[0];
		if (materialKey.equals(CLONE_MATERIAL_KEY) || materialKey.equals(REPLICATE_MATERIAL_KEY) && location != null) {
			Location cloneFrom = location.clone();
			cloneFrom.setY(cloneFrom.getY() - 1);
			setCloneLocation(cloneFrom);
		} else if (materialKey.equals(MAP_MATERIAL_KEY) || materialKey.equals(SCHEMATIC_MATERIAL_KEY)) {
			clearCloneTarget();
		} 
	}
	
	@Override
	public void setMaterial(Material material, byte data) {
		if (!mage.isRestricted(material) && material.isBlock()) {
			super.setMaterial(material, data);
			mode = BrushMode.MATERIAL;
			isValid = true;
		} else {
			isValid = false;
		}
		fillWithAir = true;
	}
	
	public void enableCloning() {
		if (this.mode != BrushMode.CLONE) {
			fillWithAir = this.mode == BrushMode.ERASE;
			this.mode = BrushMode.CLONE;
		}
	}
	
	public void enableErase() {
		if (this.mode != BrushMode.ERASE) {
			this.setMaterial(Material.AIR);
			this.mode = BrushMode.ERASE;
			fillWithAir = true;
		}
	}
	
	@SuppressWarnings("deprecation")
	public void enableMap() {
		fillWithAir = false;
		this.mode = BrushMode.MAP;
		if (this.material == Material.WOOL || this.material == Material.STAINED_CLAY
			// Use raw id's for 1.6 backwards compatibility.
			|| this.material.getId() == 95 || this.material.getId() == 160
			// || this.material == Material.STAINED_GLASS || this.material == Material.STAINED_GLASS_PANE
			|| this.material == Material.CARPET) {
			this.mapMaterialBase = this.material;
		}
	}
	
	public void enableSchematic(String name) {
		if (this.mode != BrushMode.SCHEMATIC) {
			fillWithAir = this.mode == BrushMode.ERASE;
			this.mode = BrushMode.SCHEMATIC;
		}
		this.customName = name;
		schematic = null;
	}
	
	public void clearSchematic() {
		schematic = null;
	}
	
	public void enableReplication() {
		if (this.mode != BrushMode.REPLICATE) {
			fillWithAir = this.mode == BrushMode.ERASE;
			this.mode = BrushMode.REPLICATE;
		}
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

	
	public void clearCloneLocation() {
		cloneLocation = null;
		materialTarget = null;		
	}
	
	public void clearCloneTarget() {
		cloneTarget = null;
		targetOffset = null;
		targetWorld = null;
	}
	
	public void setTargetOffset(Vector offset, World world) {
		targetOffset = offset.clone();
		targetWorld = world;
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
		setTarget(target, target);
	}
	
	public void setTarget(Location target, Location center) {
		orientVector = target.toVector().subtract(center.toVector());
		orientVector.setX(Math.abs(orientVector.getX()));
		orientVector.setY(Math.abs(orientVector.getY()));
		orientVector.setZ(Math.abs(orientVector.getZ()));
		
		if (mode == BrushMode.REPLICATE || mode == BrushMode.CLONE || mode == BrushMode.MAP || mode == BrushMode.SCHEMATIC) {
			if (cloneTarget == null || mode == BrushMode.CLONE || 
				!center.getWorld().getName().equals(cloneTarget.getWorld().getName())) {
				cloneTarget = center;
				if (targetOffset != null) {
					cloneTarget = cloneTarget.add(targetOffset);
				}
				if (targetWorld != null) {
					cloneTarget.setWorld(targetWorld);
				}
			} else if (mode == BrushMode.SCHEMATIC) {
				if (schematic != null) {
					Vector diff = target.toVector().subtract(cloneTarget.toVector());
					if (!schematic.contains(diff)) {
						cloneTarget = center;
						if (targetOffset != null) {
							cloneTarget = cloneTarget.add(targetOffset);
						}
					}
				}
			}

			if (cloneLocation == null) {
				cloneLocation = cloneTarget;
			}
			if (materialTarget == null) {
				materialTarget = cloneTarget;
			}
		}
		if (mode == BrushMode.COPY) {
			Block block = target.getBlock();
			if (targetOffset != null) {
				Location targetLocation = block.getLocation();
				targetLocation = targetLocation.add(targetOffset);
				block = targetLocation.getBlock();
			}
			updateFrom(block, mage.getRestrictedMaterials());
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
		if (mode == BrushMode.CLONE || mode == BrushMode.REPLICATE) {
			if (cloneLocation == null) {
				isValid = false;
				return true;
			}
			if (cloneTarget == null) cloneTarget = target;
			materialTarget = toTargetLocation(target);
			if (materialTarget.getY() <= 0 || materialTarget.getY() >= 255) {
				isValid = false;
			} else {
				Block block = materialTarget.getBlock();
				if (!block.getChunk().isLoaded()) return false;
	
				updateFrom(block, fromMage.getRestrictedMaterials());
				isValid = fillWithAir || material != Material.AIR;
			}
		}
		
		if (mode == BrushMode.SCHEMATIC) {
			if (schematic == null) {
				if (customName.length() == 0) {
					isValid = false;
					return true;
				}
				
				schematic = mage.getController().loadSchematic(customName);
				if (schematic == null) {
					customName = "";
					isValid = false;
					return true;
				}
			}
			if (cloneTarget == null) {
				isValid = false;
				return true;
			}
			Vector diff = target.toVector().subtract(cloneTarget.toVector());
			MaterialAndData newMaterial = schematic.getBlock(diff);
			if (newMaterial == null) {
				isValid = false;
			} else {
				updateFrom(newMaterial);
				isValid = fillWithAir || newMaterial.getMaterial() != Material.AIR;
			}
		}
		
		if (mode == BrushMode.MAP && mapId >= 0) {
			if (mapCanvas == null && fromMage != null) {
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
			isValid = false;
			if (mapCanvas != null && cloneTarget != null) {
				Vector diff = target.toVector().subtract(cloneTarget.toVector());
				
				// TODO : Different orientations, centering, scaling, etc
				// We default to 1/8 scaling for now to make the portraits work well.
				DyeColor mapColor = DyeColor.WHITE;
				if (orientVector.getBlockY() > orientVector.getBlockZ() || orientVector.getBlockY() > orientVector.getBlockX()) {
					if (orientVector.getBlockX() > orientVector.getBlockZ()) {
						mapColor = mapCanvas.getDyeColor(
								Math.abs(diff.getBlockX() * 8 + MaterialMapCanvas.CANVAS_WIDTH / 2) % MaterialMapCanvas.CANVAS_WIDTH, 
								Math.abs(-diff.getBlockY() * 8 + MaterialMapCanvas.CANVAS_HEIGHT / 2) % MaterialMapCanvas.CANVAS_HEIGHT);
					} else {
						mapColor = mapCanvas.getDyeColor(
								Math.abs(diff.getBlockZ() * 8 + MaterialMapCanvas.CANVAS_WIDTH / 2) % MaterialMapCanvas.CANVAS_WIDTH, 
								Math.abs(-diff.getBlockY() * 8 + MaterialMapCanvas.CANVAS_HEIGHT / 2) % MaterialMapCanvas.CANVAS_HEIGHT);
					}
				} else {
					mapColor = mapCanvas.getDyeColor(
						Math.abs(diff.getBlockX() * 8 + MaterialMapCanvas.CANVAS_WIDTH / 2) % MaterialMapCanvas.CANVAS_WIDTH, 
						Math.abs(diff.getBlockZ() * 8 + MaterialMapCanvas.CANVAS_HEIGHT / 2) % MaterialMapCanvas.CANVAS_HEIGHT);
				}
				if (mapColor != null) {
					super.setMaterial(mapMaterialBase, mapColor.getData());
					isValid = true;
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
			customName = node.getString("extra_data", customName);
		} catch (Exception ex) {
			ex.printStackTrace();
			mage.getController().getPlugin().getLogger().warning("Failed to load brush data: " + ex.getMessage());
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
			node.setProperty("extra_data", customName);
		} catch (Exception ex) {
			ex.printStackTrace();
			mage.getController().getLogger().warning("Failed to save brush data: " + ex.getMessage());
		}
	}
	
	public boolean hasEntities()
	{
		// return mode == BrushMode.CLONE || mode == BrushMode.REPLICATE || mode == BrushMode.SCHEMATIC;
		return mode == BrushMode.CLONE || mode == BrushMode.REPLICATE;
	}
	
	public List<EntityData> getEntities(Location center, int radius)
	{
		List<EntityData> copyEntities = new ArrayList<EntityData>();
		
		int radiusSquared = radius * radius;
		if (mode == BrushMode.CLONE || mode == BrushMode.REPLICATE)
		{
			World targetWorld = center.getWorld();
	
			// First clear all hanging entities from the area.
			List<Entity> targetEntities = targetWorld.getEntities();
			for (Entity entity : targetEntities) {
				// Specific check only for what we copy. This could be more abstract.
				if (entity instanceof Painting || entity instanceof ItemFrame) {
					if (entity.getLocation().distanceSquared(center) <= radiusSquared) {
						entity.remove();
					}
				}
			}
			
			// Now copy all hanging entities from the source location
			Location cloneLocation = toTargetLocation(center);
			World sourceWorld = cloneLocation.getWorld();
			List<Entity> entities = sourceWorld.getEntities();
			for (Entity entity : entities) {
				if (entity instanceof Painting || entity instanceof ItemFrame) {
					Location entityLocation = entity.getLocation();
					if (entity.getLocation().distanceSquared(cloneLocation) > radiusSquared) continue;
					EntityData entityData = new EntityData(fromTargetLocation(center.getWorld(), entityLocation), entity);
					copyEntities.add(entityData);
				}
			}
		} 
		else if (mode == BrushMode.SCHEMATIC)
		{
			if (schematic != null)
			{
				return schematic.getEntities(center, radius);
			}
		}
			
		return copyEntities;
	}
	
	public String getCommandLine()
	{
		return commandLine;
	}
	
	public void setCommandLine(String command)
	{
		this.commandLine = command;
	}
}
