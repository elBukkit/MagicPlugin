package com.elmakers.mine.bukkit.world.populator;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.ModifyType;
import com.elmakers.mine.bukkit.magic.MagicController;

public abstract class MagicBlockPopulator extends MagicChunkPopulator {
    private int maxY = 255;
    private int minY = 0;
    private int maxAirY = 255;
    private ModifyType modifyType = ModifyType.NO_PHYSICS;

    @Override
    public boolean load(ConfigurationSection config, MagicController controller) {
        if (!super.load(config, controller)) {
            return false;
        }
        maxY = config.getInt("max_y", maxY);
        minY = config.getInt("min_y", minY);
        maxAirY = config.getInt("max_air_y", maxAirY);
        String modifyType = config.getString("modifyType", null);
        if (modifyType != null && !modifyType.isEmpty()) {
            this.modifyType = ModifyType.valueOf(modifyType.toUpperCase());
        }
        return true;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 15; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Block block = chunk.getBlock(x,  y, z);
                    if (y > maxAirY && block.getType() == Material.AIR) {
                        break;
                    }

                    MaterialAndData newMaterial = populate(block, random);
                    if (newMaterial != null) {
                        newMaterial.modify(block, modifyType);
                    }
                }
            }
        }
    }

    public abstract MaterialAndData populate(Block block, Random random);
}
