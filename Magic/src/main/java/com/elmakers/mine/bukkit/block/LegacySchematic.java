package com.elmakers.mine.bukkit.block;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class LegacySchematic extends AbstractSchematic {

    public LegacySchematic(MageController controller) {
        super(controller);
    }

    public void load(short width, short height, short length, short[] blockTypes, byte[] data, Collection<Object> tileEntityData, Collection<Object> entityData, Vector origin, Vector offset) {
        initialize(width, height, length);
        loadEntities(entityData, origin);
        loadTileEntities(tileEntityData);

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    int index = x + (y * length + z) * width;

                    Material material = CompatibilityUtils.getMaterial(blockTypes[index], data[index]);
                    if (material != null)
                    {
                        MaterialAndData block = null;
                        // For 1.13 we're going to use BlockData here.
                        String blockData = CompatibilityUtils.getBlockData(material, data[index]);
                        if (blockData != null) {
                            block = new MaterialAndData(material, blockData);
                        } else {
                            block = new MaterialAndData(material, data[index]);
                        }

                        // Check for tile entity data
                        BlockVector blockLocation = new BlockVector(x, y, z);
                        addTileEntity(blockLocation, block);
                        blocks[x][y][z] = block;
                    }
                }
            }
        }
        loaded = true;
    }
}
