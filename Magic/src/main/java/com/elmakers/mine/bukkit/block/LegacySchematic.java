package com.elmakers.mine.bukkit.block;

import java.util.Collection;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.schematic.LoadableSchematic;

public class LegacySchematic extends AbstractSchematic implements LoadableSchematic {

    public LegacySchematic(MageController controller) {
        super(controller);
    }

    @Override
    public void load(short width, short height, short length, int[] blockTypes, byte[] data, Map<Integer, String> palette, Collection<Object> tileEntityData, Collection<Object> entityData, Vector origin) {
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
