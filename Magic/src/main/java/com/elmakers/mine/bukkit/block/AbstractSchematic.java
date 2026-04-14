package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public abstract class AbstractSchematic implements Schematic {
    private final MageController controller;
    protected volatile boolean loaded = false;
    protected Vector size;
    protected Vector center;
    protected MaterialAndData[][][] blocks;
    protected Collection<EntityData> entities;
    protected Map<BlockVector, Object> tileEntities;

    protected AbstractSchematic(MageController controller) {
        this.controller = controller;
    }

    protected void initialize(short width, short height, short length) {
        size = new Vector(width, height, length);
        center = new Vector(Math.floor(size.getBlockX() / 2), 0, Math.floor(size.getBlockZ() / 2));
        blocks = new MaterialAndData[width][height][length];
        entities = new ArrayList<>();
    }

    protected void loadTileEntities(Collection<Object> tileEntityData) {
        if (tileEntityData == null || tileEntityData.isEmpty()) return;
        tileEntities = new HashMap<>();
        for (Object tileEntity : tileEntityData)
        {
            try {
                Integer x = CompatibilityLib.getNBTUtils().getOptionalInt(tileEntity, "x");
                Integer y = CompatibilityLib.getNBTUtils().getOptionalInt(tileEntity, "y");
                Integer z = CompatibilityLib.getNBTUtils().getOptionalInt(tileEntity, "z");

                if (x == null || y == null || z == null) continue;

                BlockVector location = new BlockVector(x, y, z);
                tileEntities.put(location, tileEntity);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void addTileEntity(BlockVector blockLocation, MaterialAndData block) {
        addTileEntity(blockLocation, block, false);
    }

    protected MaterialAndData addTileEntity(BlockVector blockLocation, MaterialAndData block, boolean copy) {
        if (tileEntities == null) return block;
        Object tileEntity = tileEntities.get(blockLocation);
        if (tileEntity != null) {
            if (copy) {
                block = new MaterialAndData(block);
            }
            try {
                if (DefaultMaterials.isCommand(block.getMaterial())) {
                    String customName = CompatibilityLib.getNBTUtils().getString(tileEntity, "CustomName");
                    if (!customName.isEmpty()) {
                        block.setCustomName(customName);
                    }
                    block.setCommandLine(CompatibilityLib.getNBTUtils().getString(tileEntity, "Command"));
                } else {
                    block.setRawData(tileEntity);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return block;
    }

    protected void loadEntities(Collection<Object> entityData, Vector origin) {
        if (entityData == null || entityData.isEmpty()) return;
        for (Object entity : entityData) {
            String type = CompatibilityLib.getNBTUtils().getString(entity, "id");
            if (type == null || type.isEmpty()) {
                type = CompatibilityLib.getNBTUtils().getString(entity, "Id");
                // Not sure why this is Id here but NMS wants id?
                CompatibilityLib.getNBTUtils().setString(entity, "id", type);
                CompatibilityLib.getNBTUtils().removeMeta(entity, "Id");
            }
            Vector position = CompatibilityLib.getCompatibilityUtils().getPosition(entity, "Pos");
            if (position == null) continue;
            position = position.subtract(origin).subtract(center);

            Integer tileX = CompatibilityLib.getNBTUtils().getOptionalInt(entity, "TileX");
            if (tileX != null) {
                CompatibilityLib.getNBTUtils().setInt(entity, "TileX", tileX - center.getBlockX());
            }
            Integer tileY = CompatibilityLib.getNBTUtils().getOptionalInt(entity, "TileY");
            if (tileY != null) {
                CompatibilityLib.getNBTUtils().setInt(entity, "TileY", tileY - center.getBlockY());
            }
            Integer tileZ = CompatibilityLib.getNBTUtils().getOptionalInt(entity, "TileZ");
            if (tileZ != null) {
                CompatibilityLib.getNBTUtils().setInt(entity, "TileZ", tileZ - center.getBlockZ());
            }

            if (type == null || type.isEmpty()) continue;
            EntityData nmsEntity = com.elmakers.mine.bukkit.entity.EntityData.loadNMS(controller, position, entity);
            if (nmsEntity != null) {
                entities.add(nmsEntity);
            }
        }
    }

    @Override
    public boolean contains(Vector v) {
        int x = v.getBlockX() + center.getBlockX();
        int y = v.getBlockY() + center.getBlockY();
        int z = v.getBlockZ() + center.getBlockZ();

        return (x >= 0 && x <= size.getBlockX() && y >= 0 && y <= size.getBlockY() && z >= 0 && z <= size.getBlockZ());
    }

    @Nullable
    @Override
    public MaterialAndData getBlock(Vector v) {
        int x = v.getBlockX() + center.getBlockX();
        int y = v.getBlockY() + center.getBlockY();
        int z = v.getBlockZ() + center.getBlockZ();

        if (x < 0 || x >= blocks.length || y < 0 || y >= blocks[x].length || z < 0 || z >= blocks[x][y].length) {
            return null;
        }

        return blocks[x][y][z];
    }

    @Override
    public Collection<EntityData> getEntities(Location center) {
        List<EntityData> translated = new ArrayList<>();
        for (EntityData data : entities) {
            EntityData relative = data == null ? null : data.getRelativeTo(center);
            if (relative != null) {
                translated.add(relative);
            }
        }
        return translated;
    }

    @Override
    public Vector getSize() {
        return size;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    protected BlockFace getFacing(byte dir) {
        switch (dir) {
            case 0: return BlockFace.SOUTH;
            case 1: return BlockFace.WEST;
            case 2: return BlockFace.NORTH;
            case 3: return BlockFace.EAST;
        }
        return BlockFace.UP;
    }

}
