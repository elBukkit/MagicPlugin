package com.elmakers.mine.bukkit.blocks;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.utilities.MaterialMapCanvas;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MaterialBrush extends MaterialBrushData {
	
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
	private final MagicController controller;
	private short mapId = -1;
	private MaterialMapCanvas mapCanvas = null;
	private Material mapMaterialBase = Material.STAINED_CLAY;
	private Schematic schematic;
	private boolean fillWithAir = true;
	
	public MaterialBrush(final MagicController controller, final Material material, final  byte data) {
		super(material, data);
		this.controller = controller;
	}
	
	public MaterialBrush(final MagicController controller, final Location location, final String materialKey) {
		super(DEFAULT_MATERIAL, (byte)0);
		this.controller = controller;
		update(materialKey);
		activate(location, materialKey);
	}
	
	public static String getMaterialKey(Material material) {
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
		} else if (material.isBlock()) {
			materialKey = material.name().toLowerCase();
		}
		
		return materialKey;
	}
	
	public static boolean isSpecialMaterialKey(String materialKey) {
		if (materialKey == null || materialKey.length() == 0) return false;
		if (materialKey.contains(":")) {
			materialKey = StringUtils.split(materialKey, ":")[0];
		}
		return COPY_MATERIAL_KEY.equals(materialKey) || ERASE_MATERIAL_KEY.equals(materialKey) || 
			   REPLICATE_MATERIAL_KEY.equals(materialKey) || CLONE_MATERIAL_KEY.equals(materialKey) || 
			   MAP_MATERIAL_KEY.equals(materialKey) || (SchematicsEnabled && SCHEMATIC_MATERIAL_KEY.equals(materialKey));
	}

	@SuppressWarnings("deprecation")
	public static String getMaterialName(String materialKey) {
		if (materialKey == null) return null;
		String materialName = materialKey;
		String[] namePieces = StringUtils.split(materialName, ":");
		if (namePieces.length == 0) return null;
		
		materialName = namePieces[0];
		
		if (!MaterialBrush.isSpecialMaterialKey(materialKey)) {
			MaterialBrushData brushData = parseMaterialKey(materialKey);
			if (brushData == null) return null;
			
			Material material = brushData.getMaterial();
			byte data = brushData.getData();
			
			// This is the "right" way to do this, but relies on Bukkit actually updating Material in a timely fashion :P
			/*
			Class<? extends MaterialData> materialData = material.getData();
			Bukkit.getLogger().info("Material " + material + " has " + materialData);
			if (Wool.class.isAssignableFrom(materialData)) {
				Wool wool = new Wool(material, data);
				materialName += " " + wool.getColor().name();
			} else if (Dye.class.isAssignableFrom(materialData)) {
				Dye dye = new Dye(material, data);
				materialName += " " + dye.getColor().name();
			} else if (Dye.class.isAssignableFrom(materialData)) {
				Dye dye = new Dye(material, data);
				materialName += " " + dye.getColor().name();
			}
			*/
			
			// Using raw id's for 1.6 support... because... bukkit... bleh.
			
			//if (material == Material.CARPET || material == Material.STAINED_GLASS || material == Material.STAINED_CLAY || material == Material.STAINED_GLASS_PANE || material == Material.WOOL) {
			if (material == Material.CARPET || material.getId() == 95 || material.getId() ==159 || material.getId() == 160 || material == Material.WOOL) {
				// Note that getByDyeData doesn't work for stained glass or clay. Kind of misleading?
				DyeColor color = DyeColor.getByWoolData(data);
				materialName = color.name().toLowerCase().replace('_', ' ') + " " + materialName;
			} else if (material == Material.WOOD || material == Material.LOG || material == Material.SAPLING || material == Material.LEAVES) {
				TreeSpecies treeSpecies = TreeSpecies.getByData(data);
				materialName = treeSpecies.name().toLowerCase().replace('_', ' ') + " " + materialName;
			} else {
				materialName = material.name();				
			}
		} else if (materialName.startsWith(MaterialBrush.SCHEMATIC_MATERIAL_KEY) && namePieces.length > 1) {
			materialName = namePieces[1];
		}
		
		materialName = materialName.toLowerCase().replace('_', ' ');
		
		return materialName;
	}
	
	@SuppressWarnings("deprecation")
	public static MaterialBrushData parseMaterialKey(String materialKey) {
		if (materialKey == null || materialKey.length() == 0) return null;
		
		Material material = Material.DIRT;
		byte data = 0;
		String schematicName = "";
		String[] pieces = StringUtils.split(materialKey, ":");
				
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
		} else if (SchematicsEnabled && pieces[0].equals(SCHEMATIC_MATERIAL_KEY)) {
			material = SchematicMaterial;
			schematicName = pieces[1];
		} else {
			try {
				if (pieces.length > 0) {
					// Legacy material id loading
					try {
						Integer id = Integer.parseInt(pieces[0]);
						material = Material.getMaterial(id);
					} catch (Exception ex) {
						material = Material.getMaterial(pieces[0].toUpperCase());
					}
				}
				
				// Prevent building with items
				if (material != null && !material.isBlock()) {
					material = null;
				}
			} catch (Exception ex) {
				material = null;
			}
			try {
				if (pieces.length > 1) {
					data = Byte.parseByte(pieces[1]);
			}
			} catch (Exception ex) {
				data = 0;
			}
		}
		if (material == null) return null;
		return new MaterialBrushData(material, data, schematicName);
	}
	
	public static boolean isValidMaterial(String materialKey) {
		return parseMaterialKey(materialKey) != null;
	}

	public void update(String activeMaterial) {
		String pieces[] = StringUtils.split(activeMaterial, ":");
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
		String materialKey = material;
		if (materialKey.contains(":")) {
			materialKey = StringUtils.split(materialKey, ":")[0];
		}
		if (materialKey.equals(CLONE_MATERIAL_KEY) || materialKey.equals(REPLICATE_MATERIAL_KEY)) {
			Location cloneFrom = location.clone();
			cloneFrom.setY(cloneFrom.getY() - 1);
			setCloneLocation(cloneFrom);
		} else if (materialKey.equals(MAP_MATERIAL_KEY) || materialKey.equals(SCHEMATIC_MATERIAL_KEY)) {
			clearCloneTarget();
		} 
	}
	
	@Override
	public void setMaterial(Material material, byte data) {
		if (!controller.isRestricted(material) && material.isBlock()) {
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
	
	public void enableMap() {
		fillWithAir = false;
		this.mode = BrushMode.MAP;
		if (this.material == Material.WOOL || this.material == Material.STAINED_CLAY
			|| this.material == Material.STAINED_GLASS || this.material == Material.STAINED_GLASS_PANE
			|| this.material == Material.CARPET) {
			this.mapMaterialBase = this.material;
		}
	}
	
	public void enableSchematic(String name) {
		if (this.mode != BrushMode.SCHEMATIC) {
			fillWithAir = this.mode == BrushMode.ERASE;
			this.mode = BrushMode.SCHEMATIC;
		}
		this.schematicName = name;
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
		setTarget(target, target);
	}
	
	public void setTarget(Location target, Location center) {
		if (mode == BrushMode.REPLICATE || mode == BrushMode.CLONE || mode == BrushMode.MAP || mode == BrushMode.SCHEMATIC) {
			if (cloneTarget == null || mode == BrushMode.CLONE || 
				!center.getWorld().getName().equals(cloneTarget.getWorld().getName())) {
				cloneTarget = center;
			} else if (mode == BrushMode.SCHEMATIC) {
				if (schematic == null) {
					cloneTarget = center;
				} else {
					Vector diff = target.toVector().subtract(cloneTarget.toVector());
					if (!schematic.contains(diff)) {
						cloneTarget = center;
					}
				}
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
	
				updateFrom(block, controller.getRestrictedMaterials());
				isValid = fillWithAir || material != Material.AIR;
			}
		}
		
		if (mode == BrushMode.SCHEMATIC) {
			if (schematic == null) {
				if (schematicName.length() == 0) {
					isValid = false;
					return true;
				}
				
				schematic = controller.loadSchematic(schematicName);
				if (schematic == null) {
					schematicName = "";
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
				
				// TODO : Different orientations, centering, etc
				DyeColor mapColor = mapCanvas.getDyeColor(
						Math.abs(diff.getBlockX() + MaterialMapCanvas.CANVAS_WIDTH / 2) % MaterialMapCanvas.CANVAS_WIDTH, 
						Math.abs(diff.getBlockZ() + MaterialMapCanvas.CANVAS_HEIGHT / 2) % MaterialMapCanvas.CANVAS_HEIGHT);
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
			schematicName = node.getString("schematic", schematicName);
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
			node.setProperty("schematic", schematicName);
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
