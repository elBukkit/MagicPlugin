package com.elmakers.mine.bukkit.block;

import java.util.Collection;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class Schematic  extends AbstractSchematic {
    public void load(short width, short height, short length, byte[] blockTypes, Map<Integer, Material> materialPalette, Map<Integer, String> dataPalette, Collection<Object> tileEntityData, Collection<Object> entityData, Vector origin) {
        initialize(width, height, length);
        loadEntities(entityData, origin);
        loadTileEntities(tileEntityData);

        if (blockTypes != null && materialPalette != null && dataPalette != null) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int index = x + (y * length + z) * width;
                        int palleteIndex = blockTypes[index];
                        String blockData = dataPalette.get(palleteIndex);
                        Material material = materialPalette.get(palleteIndex);
                        if (material != null) {
                            MaterialAndData block = new MaterialAndData(material, blockData);
                            // Check for tile entity data
                            BlockVector blockLocation = new BlockVector(x, y, z);
                            addTileEntity(blockLocation, block);
                            blocks[x][y][z] = block;
                        }
                    }
                }
            }
        }
        loaded = true;
    }
}
