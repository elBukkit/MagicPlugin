package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.elmakers.mine.bukkit.api.block.BrushMode;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.entity.EntityData;
import com.elmakers.mine.bukkit.maps.BufferedMapCanvas;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MaterialBrush extends MaterialAndData implements com.elmakers.mine.bukkit.api.block.MaterialBrush {

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
    private Location cloneSource = null;
    private Location cloneTarget = null;
    private Location materialTarget = null;
    private Vector targetOffset = null;
    private String targetWorldName = null;
    private final Mage mage;
    private short mapId = -1;
    private BufferedMapCanvas mapCanvas = null;
    private Material mapMaterialBase = Material.STAINED_CLAY;
    private Schematic schematic;
    private String schematicName = "";
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

        if (materialName.startsWith(MaterialBrush.SCHEMATIC_MATERIAL_KEY) && namePieces.length > 1) {
            materialName = namePieces[1];
        } else if (!MaterialBrush.isSpecialMaterialKey(materialKey)) {
            return MaterialAndData.getMaterialName(materialKey);
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
        case SCHEMATIC: brushKey = SCHEMATIC_MATERIAL_KEY + ":" + schematicName; break;
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
            // TODO: Kind of a hack, but I think I did this for display reasons? Need to circle back on it.
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
        this.schematicName = name;
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
        cloneSource = cloneFrom;
        materialTarget = cloneFrom;
        cloneTarget = null;
    }

    public void clearCloneLocation() {
        cloneSource = null;
        materialTarget = null;
    }

    public void clearCloneTarget() {
        cloneTarget = null;
        targetOffset = null;
        targetWorldName = null;
    }

    public void setTargetOffset(Vector offset, String worldName) {
        targetOffset = offset.clone();
        targetWorldName = worldName;
    }

    public boolean hasCloneTarget() {
        return cloneSource != null && cloneTarget != null;
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

    public Location toTargetLocation(Location target) {
        if (cloneSource == null || cloneTarget == null) return null;
        Location translated = cloneSource.clone();
        translated.subtract(cloneTarget.toVector());
        translated.add(target.toVector());
        return translated;
    }

    public Location fromTargetLocation(World targetWorld, Location target) {
        if (cloneSource == null || cloneTarget == null) return null;
        Location translated = target.clone();
        translated.setX(translated.getBlockX());
        translated.setY(translated.getBlockY());
        translated.setZ(translated.getBlockZ());
        Vector cloneVector = new Vector(cloneSource.getBlockX(), cloneSource.getBlockY(), cloneSource.getBlockZ());
        translated.subtract(cloneVector);
        Vector cloneTargetVector = new Vector(cloneTarget.getBlockX(), cloneTarget.getBlockY(), cloneTarget.getBlockZ());
        translated.add(cloneTargetVector);
        translated.setWorld(targetWorld);
        return translated;
    }

    @SuppressWarnings("deprecation")
    public boolean update(final Mage fromMage, final Location target) {
        if (mode == BrushMode.CLONE || mode == BrushMode.REPLICATE) {
            if (cloneSource == null) {
                isValid = false;
                return true;
            }
            if (cloneTarget == null) cloneTarget = target;
            materialTarget = toTargetLocation(target);
            if (materialTarget.getY() < 0 || materialTarget.getY() > fromMage.getController().getMaxY()) {
                isValid = false;
            } else {
                Block block = materialTarget.getBlock();
                if (!block.getChunk().isLoaded()) return false;

                updateFrom(block, fromMage.getRestrictedMaterials());
                isValid = fillWithAir || material != Material.AIR;
            }
        }

        if (mode == BrushMode.SCHEMATIC) {
            if (!checkSchematic()) {
                return true;
            }
            if (cloneTarget == null) {
                isValid = false;
                return true;
            }
            Vector diff = target.toVector().subtract(cloneTarget.toVector());
            com.elmakers.mine.bukkit.api.block.MaterialAndData newMaterial = schematic.getBlock(diff);
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
                        Player player = fromMage.getPlayer();
                        List<MapRenderer> renderers = mapView.getRenderers();
                        if (renderers.size() > 0 && player != null) {
                            mapCanvas = new BufferedMapCanvas();
                            MapRenderer renderer = renderers.get(0);
                            // This is mainly here as a hack for my own urlmaps that do their own caching
                            // Bukkit *seems* to want to do caching at the MapView level, but looking at the code-
                            // they cache but never use the cache?
                            // Anyway render gets called constantly so I'm not re-rendering on each render... but then
                            // how to force a render to a canvas? So we re-initialize.
                            renderer.initialize(mapView);
                            renderer.render(mapView, mapCanvas, player);
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
                                Math.abs(diff.getBlockX() * 8 + BufferedMapCanvas.CANVAS_WIDTH / 2) % BufferedMapCanvas.CANVAS_WIDTH,
                                Math.abs(-diff.getBlockY() * 8 + BufferedMapCanvas.CANVAS_HEIGHT / 2) % BufferedMapCanvas.CANVAS_HEIGHT);
                    } else {
                        mapColor = mapCanvas.getDyeColor(
                                Math.abs(diff.getBlockZ() * 8 + BufferedMapCanvas.CANVAS_WIDTH / 2) % BufferedMapCanvas.CANVAS_WIDTH,
                                Math.abs(-diff.getBlockY() * 8 + BufferedMapCanvas.CANVAS_HEIGHT / 2) % BufferedMapCanvas.CANVAS_HEIGHT);
                    }
                } else {
                    mapColor = mapCanvas.getDyeColor(
                        Math.abs(diff.getBlockX() * 8 + BufferedMapCanvas.CANVAS_WIDTH / 2) % BufferedMapCanvas.CANVAS_WIDTH,
                        Math.abs(diff.getBlockZ() * 8 + BufferedMapCanvas.CANVAS_HEIGHT / 2) % BufferedMapCanvas.CANVAS_HEIGHT);
                }
                if (mapColor != null) {
                    super.setMaterial(mapMaterialBase, mapColor.getData());
                    isValid = true;
                }
            }
        }

        return true;
    }

    protected boolean checkSchematic() {
        if (schematic == null) {
            if (schematicName.length() == 0) {
                isValid = false;
                return false;
            }

            schematic = mage.getController().loadSchematic(schematicName);
            if (schematic == null) {
                schematicName = "";
                isValid = false;
                return false;
            }
        }

        return schematic != null;
    }

    public void prepare() {
        if (cloneSource != null) {
            Block block = cloneTarget.getBlock();
            if (!block.getChunk().isLoaded()) {
                block.getChunk().load(true);
            }
        }
    }

    public void load(ConfigurationSection node)
    {
        try {
            cloneSource = ConfigurationUtils.getLocation(node, "clone_location");
            cloneTarget = ConfigurationUtils.getLocation(node, "clone_target");
            materialTarget = ConfigurationUtils.getLocation(node, "material_target");
            schematicName = node.getString("schematic", schematicName);
            mapId = (short)node.getInt("map_id", mapId);
            material = ConfigurationUtils.getMaterial(node, "material", material);
            data = (byte)node.getInt("data", data);
            customName = node.getString("extra_data", customName);
        } catch (Exception ex) {
            ex.printStackTrace();
            mage.getController().getLogger().warning("Failed to load brush data: " + ex.getMessage());
        }
    }

    public void save(ConfigurationSection node)
    {
        try {
            if (cloneSource != null) {
                node.set("clone_location", ConfigurationUtils.fromLocation(cloneSource));
            }
            if (cloneTarget != null) {
                node.set("clone_target", ConfigurationUtils.fromLocation(cloneTarget));
            }
            if (materialTarget != null) {
                node.set("material_target", ConfigurationUtils.fromLocation(materialTarget));
            }
            node.set("map_id", (int)mapId);
            node.set("material", ConfigurationUtils.fromMaterial(material));
            node.set("data", data);
            node.set("extra_data", customName);
            node.set("schematic", schematicName);
        } catch (Exception ex) {
            ex.printStackTrace();
            mage.getController().getLogger().warning("Failed to save brush data: " + ex.getMessage());
        }
    }

    @Override
    public boolean hasEntities()
    {
        // return mode == BrushMode.CLONE || mode == BrushMode.REPLICATE || mode == BrushMode.SCHEMATIC;
        return mode == BrushMode.CLONE || mode == BrushMode.REPLICATE;
    }

    @Override
    public Collection<Entity> getTargetEntities()
    {
        if (cloneTarget == null) return null;

        // TODO: Add SCHEMATIC here once we're adding schematic entities
        if (mode == BrushMode.CLONE || mode == BrushMode.REPLICATE)
        {
            List<Entity> targetData = new ArrayList<Entity>();
            World targetWorld = cloneTarget.getWorld();
            List<Entity> targetEntities = targetWorld.getEntities();
            for (Entity entity : targetEntities) {
                // Note that we'll clear Item entities even though we can't respawn them!
                // Also note that we ignore players and NPCs
                if (!(entity instanceof Player) && !mage.getController().isNPC(entity)) {
                    targetData.add(entity);
                }
            }

            return targetData;
        }

        return null;
    }

    @Override
    public Collection<com.elmakers.mine.bukkit.api.entity.EntityData> getEntities()
    {
        if (cloneTarget == null || cloneSource == null) return null;

        if (mode == BrushMode.CLONE || mode == BrushMode.REPLICATE)
        {
            List<com.elmakers.mine.bukkit.api.entity.EntityData> copyEntities = new ArrayList<com.elmakers.mine.bukkit.api.entity.EntityData>();

            World sourceWorld = cloneSource.getWorld();
            List<Entity> entities = sourceWorld.getEntities();
            for (Entity entity : entities) {
                if (!(entity instanceof Player || entity instanceof Item)) {
                    Location entityLocation = entity.getLocation();
                    Location translated = fromTargetLocation(cloneTarget.getWorld(), entityLocation);
                    EntityData entityData = new EntityData(translated, entity);
                    copyEntities.add(entityData);
                }
            }

            return copyEntities;
        }
        else if (mode == BrushMode.SCHEMATIC)
        {
            if (schematic != null)
            {
                return schematic.getEntities();
            }
        }

        return null;
    }

    @Override
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

    @Override
    public void setTarget(Location target) {
        setTarget(target, target);
    }

    @Override
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
            } else if (mode == BrushMode.SCHEMATIC) {
                if (schematic == null && schematicName != null) {
                    schematic = mage.getController().loadSchematic(schematicName);
                }
                boolean recenter = true;

                if (schematic != null) {
                    Vector diff = target.toVector().subtract(cloneTarget.toVector());
                    recenter = (!schematic.contains(diff));
                }

                if (recenter) {
                    cloneTarget = center;
                    if (targetOffset != null) {
                        cloneTarget = cloneTarget.add(targetOffset);
                    }
                }
            }

            if (cloneSource == null) {
                cloneSource = cloneTarget.clone();
                if (targetWorldName != null && targetWorldName.length() > 0) {
                    World sourceWorld = cloneSource.getWorld();
                    cloneSource.setWorld(ConfigurationUtils.overrideWorld(targetWorldName, sourceWorld, mage.getController().canCreateWorlds()));
                }
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

    @Override
    public Vector getSize() {
        if (mode != BrushMode.SCHEMATIC) {
            return new Vector(0, 0, 0);
        }

        if (!checkSchematic()) {
            return new Vector(0, 0, 0);
        }

        return schematic.getSize();
    }

    @Override
    public BrushMode getMode()
    {
        return mode;
    }

    @Override
    public boolean isEraseModifierActive()
    {
        return fillWithAir;
    }
}
