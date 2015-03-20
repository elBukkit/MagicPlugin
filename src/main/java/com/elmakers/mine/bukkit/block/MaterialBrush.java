package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.elmakers.mine.bukkit.api.block.BrushMode;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    public static final int DEFAULT_MAP_SIZE = 16;

    // This does not include schematics
    public static final String[] SPECIAL_MATERIAL_KEYS = {ERASE_MATERIAL_KEY, COPY_MATERIAL_KEY,
        CLONE_MATERIAL_KEY, REPLICATE_MATERIAL_KEY, MAP_MATERIAL_KEY};

    public static Material EraseMaterial = Material.SULPHUR;
    public static Material CopyMaterial = Material.SUGAR;
    public static Material CloneMaterial = Material.NETHER_STALK;
    public static Material ReplicateMaterial = Material.PUMPKIN_SEEDS;
    public static Material MapMaterial = Material.MAP;
    public static Material SchematicMaterial = Material.PAPER;
    public static Material DefaultBrushMaterial = Material.SULPHUR;

    public static String EraseCustomIcon;
    public static String CopyCustomIcon;
    public static String CloneCustomIcon;
    public static String ReplicateCustomIcon;
    public static String MapCustomIcon;
    public static String SchematicCustomIcon;
    public static String DefaultBrushCustomIcon;

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
    private double scale = 1;

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

    // Used only for generating names
    private MaterialBrush(final String materialKey) {
        super(DEFAULT_MATERIAL, (byte)0);
        this.mage = null;
        update(materialKey);
    }

    public String getKey() {
        String materialKey = null;
        if (mode == BrushMode.ERASE) {
            materialKey = ERASE_MATERIAL_KEY;
        } else if (mode == BrushMode.COPY) {
            materialKey = COPY_MATERIAL_KEY;
        } else if (mode == BrushMode.CLONE) {
            materialKey = CLONE_MATERIAL_KEY;
        } else if (mode == BrushMode.MAP) {
            materialKey = MAP_MATERIAL_KEY;
            int mapSize = (int)((float)128 / scale);
            if (mapSize != DEFAULT_MAP_SIZE)
            {
                materialKey = materialKey + ":" + mapSize;
            }
        } else if (mode == BrushMode.REPLICATE) {
            materialKey = REPLICATE_MATERIAL_KEY;
        } else if (SchematicsEnabled && mode == BrushMode.SCHEMATIC) {
            // This would be kinda broken.. might want to revisit all this.
            // This method is only called by addMaterial at this point,
            // which should only be called with real materials anyway.
            materialKey = SCHEMATIC_MATERIAL_KEY + ":" + schematicName;
        } else {
            materialKey = super.getKey();
        }

        return materialKey;
    }

    public static boolean isSpecialMaterialKey(String materialKey) {
        if (materialKey == null || materialKey.length() == 0) return false;
        materialKey = splitMaterialKey(materialKey)[0];
        return COPY_MATERIAL_KEY.equals(materialKey) || ERASE_MATERIAL_KEY.equals(materialKey) ||
               REPLICATE_MATERIAL_KEY.equals(materialKey) || CLONE_MATERIAL_KEY.equals(materialKey) ||
               MAP_MATERIAL_KEY.equals(materialKey) || (SchematicsEnabled && SCHEMATIC_MATERIAL_KEY.equals(materialKey));
    }

    public static boolean isSchematic(String materialKey) {
        if (materialKey == null || materialKey.length() == 0) return false;
        materialKey = splitMaterialKey(materialKey)[0];
        return SCHEMATIC_MATERIAL_KEY.equals(materialKey);
    }

    public static String getMaterialName(Messages messages, String materialKey) {
        MaterialBrush brush = new MaterialBrush(materialKey);
        return brush.getName(messages);
    }

    public String getName() {
        Messages messages = mage != null ? mage.getController().getMessages() : null;
        return getName(messages);
    }

    public String getName(Messages messages) {
        String brushKey;
        switch (mode) {
        case ERASE:
            brushKey = ERASE_MATERIAL_KEY;
            if (messages != null) {
                brushKey = messages.get("wand.erase_material_name");
            }
            break;
        case CLONE:
            brushKey = CLONE_MATERIAL_KEY;
            if (messages != null) {
                brushKey = messages.get("wand.clone_material_name");
            }
            break;
        case REPLICATE:
            brushKey = REPLICATE_MATERIAL_KEY;
            if (messages != null) {
                brushKey = messages.get("wand.replicate_material_name");
            }
            break;
        case COPY:
            brushKey = COPY_MATERIAL_KEY;
            if (messages != null) {
                brushKey = messages.get("wand.copy_material_name");
            }
            break;
        case MAP:
            brushKey = MAP_MATERIAL_KEY;
            int mapSize = (int)((float)128 / scale);
            if (mapSize != DEFAULT_MAP_SIZE)
            {
                if (messages != null) {
                    brushKey = messages.get("wand.map_material_name_scaled");
                    brushKey = brushKey.replace("$size", Integer.toString(mapSize));
                } else {
                    brushKey = brushKey + " " + mapSize + "x" + mapSize;
                }
            } else if (messages != null) {
                brushKey = messages.get("wand.map_material_name");
            }
            break;
        case SCHEMATIC:
            brushKey = schematicName;
            brushKey = brushKey.toLowerCase().replace('_', ' ');
            break;
        default:
            brushKey = super.getName(messages);
        }

        return brushKey;
    }

    public static MaterialBrush parseMaterialKey(String materialKey) {
        return parseMaterialKey(materialKey, false);
    }

    public static MaterialBrush parseMaterialKey(String materialKey, boolean allowItems) {
        if (materialKey == null || materialKey.length() == 0) return null;
        MaterialBrush brush = new MaterialBrush(materialKey);
        return brush.isValid(allowItems) ? brush : null;
    }

    public static boolean isValidMaterial(String materialKey, boolean allowItems) {
        MaterialBrush brush = new MaterialBrush(materialKey);
        return brush.isValid(allowItems);
    }

    public boolean isValid(boolean allowItems) {
        if (!isValid()) return false;
        if (mode != BrushMode.MATERIAL) return true;
        return allowItems || material.isBlock();
    }

    @Override
    public void setMaterial(Material material, Short data) {
        if (material != null && material.isBlock() && (mage == null || !mage.isRestricted(material))) {
            super.setMaterial(material, data);
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
    public void enableMap(int size) {
        fillWithAir = false;
        if (size <= 0) {
            size = DEFAULT_MAP_SIZE;
        }
        this.scale = (float)128 / size;
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
            if (materialTarget.getY() < 0 || materialTarget.getY() > fromMage.getController().getMaxY() || materialTarget.getWorld() == null) {
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
                                Math.abs((int)(diff.getBlockX() * scale + BufferedMapCanvas.CANVAS_WIDTH / 2) % BufferedMapCanvas.CANVAS_WIDTH),
                                Math.abs((int)(-diff.getBlockY() * scale + BufferedMapCanvas.CANVAS_HEIGHT / 2) % BufferedMapCanvas.CANVAS_HEIGHT));
                    } else {
                        mapColor = mapCanvas.getDyeColor(
                                Math.abs((int)(diff.getBlockZ() * scale + BufferedMapCanvas.CANVAS_WIDTH / 2) % BufferedMapCanvas.CANVAS_WIDTH),
                                Math.abs((int)(-diff.getBlockY() * scale + BufferedMapCanvas.CANVAS_HEIGHT / 2) % BufferedMapCanvas.CANVAS_HEIGHT));
                    }
                } else {
                    mapColor = mapCanvas.getDyeColor((int)(
                        Math.abs((int)(diff.getBlockX() * scale + BufferedMapCanvas.CANVAS_WIDTH / 2) % BufferedMapCanvas.CANVAS_WIDTH)),
                        Math.abs((int)(diff.getBlockZ() * scale + BufferedMapCanvas.CANVAS_HEIGHT / 2) % BufferedMapCanvas.CANVAS_HEIGHT));
                }
                if (mapColor != null) {
                    this.material = mapMaterialBase;
                    this.data = (short)mapColor.getData();
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
            data = (short)node.getInt("data", data);
            customName = node.getString("extra_data", customName);
            scale = node.getDouble("scale", scale);
            fillWithAir = node.getBoolean("erase", fillWithAir);
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
            node.set("scale", scale);
            node.set("erase", fillWithAir);
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
        isValid = true;
        if (activeMaterial.equals(COPY_MATERIAL_KEY)) {
            enableCopying();
        } else if (activeMaterial.equals(CLONE_MATERIAL_KEY)) {
            enableCloning();
        } else if (activeMaterial.equals(REPLICATE_MATERIAL_KEY)) {
            enableReplication();
        } else if (pieces[0].equals(MAP_MATERIAL_KEY)) {
            int size = DEFAULT_MAP_SIZE;
            if (pieces.length > 1) {
                try {
                    size = Integer.parseInt(pieces[1]);
                } catch (Exception ex) {
                }
            }
            enableMap(size);
        } else if (activeMaterial.equals(ERASE_MATERIAL_KEY)) {
            enableErase();
        } else if (pieces.length > 1 && pieces[0].equals(SCHEMATIC_MATERIAL_KEY)) {
            enableSchematic(pieces[1]);
        } else {
            mode = BrushMode.MATERIAL;
            super.update(activeMaterial);
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

    public ItemStack getItem(MageController controller, boolean isItem) {
        Messages messages = controller.getMessages();
        Material material = this.getMaterial();
        short dataId = this.getData();
        String extraLore = null;
        String customName = getName(messages);
        ItemStack itemStack = null;

        if (mode == BrushMode.ERASE) {
            material = MaterialBrush.EraseMaterial;
            if (EraseCustomIcon != null && !EraseCustomIcon.isEmpty() && controller.isUrlIconsEnabled()) {
                itemStack = InventoryUtils.getURLSkull(EraseCustomIcon);
            }
            extraLore = messages.get("wand.erase_material_description");
        } else if (mode == BrushMode.COPY) {
            material = MaterialBrush.CopyMaterial;
            if (CopyCustomIcon != null && !CopyCustomIcon.isEmpty() && controller.isUrlIconsEnabled()) {
                itemStack = InventoryUtils.getURLSkull(CopyCustomIcon);
            }
            extraLore = messages.get("wand.copy_material_description");
        } else if (mode == BrushMode.CLONE) {
            material = MaterialBrush.CloneMaterial;
            if (CloneCustomIcon != null && !CloneCustomIcon.isEmpty() && controller.isUrlIconsEnabled()) {
                itemStack = InventoryUtils.getURLSkull(CloneCustomIcon);
            }
            extraLore = messages.get("wand.clone_material_description");
        } else if (mode == BrushMode.REPLICATE) {
            material = MaterialBrush.ReplicateMaterial;
            if (ReplicateCustomIcon != null && !ReplicateCustomIcon.isEmpty() && controller.isUrlIconsEnabled()) {
                itemStack = InventoryUtils.getURLSkull(ReplicateCustomIcon);
            }
            extraLore = messages.get("wand.replicate_material_description");
        } else if (mode == BrushMode.MAP) {
            material = MaterialBrush.MapMaterial;
            if (MapCustomIcon != null && !MapCustomIcon.isEmpty() && controller.isUrlIconsEnabled()) {
                itemStack = InventoryUtils.getURLSkull(MapCustomIcon);
            }
            extraLore = messages.get("wand.map_material_description");
        } else if (mode == BrushMode.SCHEMATIC) {
            material = MaterialBrush.SchematicMaterial;
            if (SchematicCustomIcon != null && !SchematicCustomIcon.isEmpty() && controller.isUrlIconsEnabled()) {
                itemStack = InventoryUtils.getURLSkull(SchematicCustomIcon);
            }
            extraLore = messages.get("wand.schematic_material_description").replace("$schematic", schematicName);
        } else {
            if (material == Material.WATER || material == Material.STATIONARY_WATER || material == Material.LAVA || material == Material.STATIONARY_LAVA) {
                if (material == Material.WATER || material == Material.STATIONARY_WATER) {
                    material = Material.WATER_BUCKET;
                } else if (material == Material.LAVA || material == Material.STATIONARY_LAVA) {
                    material = Material.LAVA_BUCKET;
                }
            }
            extraLore = messages.get("wand.building_material_description").replace("$material", customName);
        }

        if (itemStack == null) {
            itemStack = new ItemStack(material, 1, dataId);
            itemStack = InventoryUtils.makeReal(itemStack);
            if (itemStack == null) {
                if (DefaultBrushCustomIcon != null && !DefaultBrushCustomIcon.isEmpty() && controller.isUrlIconsEnabled()) {
                    itemStack = InventoryUtils.getURLSkull(DefaultBrushCustomIcon);
                }
                if (itemStack == null) {
                    itemStack = new ItemStack(DefaultBrushMaterial, 1, dataId);
                    itemStack = InventoryUtils.makeReal(itemStack);
                    if (itemStack == null) {
                        return itemStack;
                    }
                }
            }
        }
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<String>();
        if (extraLore != null) {
            lore.add(ChatColor.LIGHT_PURPLE + extraLore);
        }
        if (isItem) {
            lore.add(ChatColor.YELLOW + messages.get("wand.brush_item_description"));
        }
        meta.setLore(lore);
        if (customName != null) {
            meta.setDisplayName(customName);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
