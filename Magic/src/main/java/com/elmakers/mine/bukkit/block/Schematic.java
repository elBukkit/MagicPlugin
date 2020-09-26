package com.elmakers.mine.bukkit.block;

import java.util.Collection;
import java.util.Map;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class Schematic  extends AbstractSchematic {
    public void load(short width, short height, short length, int[] blockTypes, Map<Integer, MaterialAndData> palette, Collection<Object> tileEntityData, Collection<Object> entityData, Vector origin) {
        initialize(width, height, length);
        loadEntities(entityData, origin);
        loadTileEntities(tileEntityData);

        if (blockTypes != null && palette != null) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int index = x + (y * length + z) * width;
                        int palleteIndex = blockTypes[index];
                        MaterialAndData material = palette.get(palleteIndex);
                        if (material != null) {
                            // Check for tile entity data
                            BlockVector blockLocation = new BlockVector(x, y, z);
                            addTileEntity(blockLocation, material);
                            blocks[x][y][z] = material;
                        }
                    }
                }
            }
        }
        loaded = true;
    }
}
