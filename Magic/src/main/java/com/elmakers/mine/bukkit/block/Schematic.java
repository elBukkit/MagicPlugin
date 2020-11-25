package com.elmakers.mine.bukkit.block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class Schematic  extends AbstractSchematic {
    public Schematic(MageController controller) {
        super(controller);
    }

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
                            material = addTileEntity(blockLocation, material, true);
                            blocks[x][y][z] = material;
                        }
                    }
                }
            }
        }
        loaded = true;
    }

    @Override
    protected void loadTileEntities(Collection<Object> tileEntityData) {
        if (tileEntityData == null || tileEntityData.isEmpty()) return;
        tileEntities = new HashMap<>();
        for (Object tileEntity : tileEntityData)
        {
            try {
                BlockVector position = NMSUtils.getBlockVector(tileEntity, "Pos");
                if (position == null) continue;
                tileEntities.put(position, tileEntity);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
