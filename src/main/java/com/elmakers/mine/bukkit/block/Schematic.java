package com.elmakers.mine.bukkit.block;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Schematic implements com.elmakers.mine.bukkit.api.block.Schematic {
    private final Vector size;
    private final Vector center;
    private final MaterialAndData blocks[][][];
    private final Collection<EntityData> entities;

    @SuppressWarnings("deprecation")
    public Schematic(short width, short height, short length, short[] blockTypes, byte[] data, Collection<Object> tileEntityData, Collection<Object> entityData) {
        size = new Vector(width, height, length);
        center = new Vector(Math.floor(size.getBlockX() / 2), 0, Math.floor(size.getBlockZ() / 2));
        blocks = new MaterialAndData[width][height][length];
        entities = new ArrayList<EntityData>();

        // Map tile entity data
        Map<BlockVector, Object> tileEntityMap = new HashMap<BlockVector, Object>();
        for (Object tileEntity : tileEntityData)
        {
            try {
                Integer x = NMSUtils.getMetaInt(tileEntity, "x");
                Integer y = NMSUtils.getMetaInt(tileEntity, "y");
                Integer z = NMSUtils.getMetaInt(tileEntity, "z");

                if (x == null || y == null || z == null) continue;

                BlockVector location = new BlockVector(x, y, z);
                tileEntityMap.put(location, tileEntity);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        for(int y = 0; y < height; y++)
        {
            for(int z = 0; z < length; z++)
            {
                for(int x = 0; x < width; x++)
                {
                    int index = x + (y * length + z) * width;
                    Material material = null;
                    try {
                        material = Material.getMaterial(blockTypes[index]);
                    } catch (Exception ex) {
                        material = null;
                        ex.printStackTrace();
                    }

                    if (material != null)
                    {
                        MaterialAndData block = new com.elmakers.mine.bukkit.block.MaterialAndData(material, data[index]);

                        // Check for tile entity data
                        BlockVector blockLocation = new BlockVector(x, y, z);
                        Object tileEntity = tileEntityMap.get(blockLocation);
                        if (tileEntity != null) {
                            try {
                                if (material == Material.SIGN_POST || material == Material.WALL_SIGN) {
                                    String[] lines = new String[4];
                                    lines[0] = NMSUtils.getMeta(tileEntity, "Text1");
                                    lines[1] = NMSUtils.getMeta(tileEntity, "Text2");
                                    lines[2] = NMSUtils.getMeta(tileEntity, "Text3");
                                    lines[3] = NMSUtils.getMeta(tileEntity, "Text4");
                                    block.setSignLines(lines);
                                } else if (material == Material.COMMAND) {
                                    String customName = NMSUtils.getMeta(tileEntity, "CustomName");
                                    if (!customName.isEmpty()) {
                                        block.setCustomName(customName);
                                    }
                                    block.setCommandLine(NMSUtils.getMeta(tileEntity, "Command"));
                                } else if (NMSUtils.containsNode(tileEntity, "Items")) {
                                    ItemStack[] items = NMSUtils.getItems(tileEntity, "Items");
                                    if (items != null) {
                                        block.setInventoryContents(items);
                                    }
                                } else {
                                    block.setRawData(tileEntity);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        blocks[x][y][z] = block;
                    }
                }
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
    public Collection<EntityData> getEntities() {
        return entities;
    }

    @Override
    public Vector getSize() {
        return size;
    }
}
