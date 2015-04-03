package com.elmakers.mine.bukkit.block;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;

public class Schematic implements com.elmakers.mine.bukkit.api.block.Schematic {
    private final Vector size;
    private final Vector center;
    private final MaterialAndData blocks[][][];

    public Schematic(short width, short height, short length, byte[] blockTypes, byte[] data) {
        size = new Vector(width, height, length);
        center = new Vector(Math.floor(size.getBlockX() / 2), 0, Math.floor(size.getBlockZ() / 2));
        blocks = new MaterialAndData[width][height][length];

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
                        ex.printStackTrace();;
                    }
                    if (material != null)
                    {
                        blocks[x][y][z] = new com.elmakers.mine.bukkit.block.MaterialAndData(material, data[index]);
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

        if (x < 0 || x >= size.getBlockZ() || y < 0 || y >= size.getBlockY() || z < 0 || z >= size.getBlockZ()) {
            return null;
        }

        return blocks[x][y][z];
    }

    @Override
    public Collection<EntityData> getEntities() {
        return new ArrayList<EntityData>();
    }

    @Override
    public Vector getSize() {
        return size;
    }
}
