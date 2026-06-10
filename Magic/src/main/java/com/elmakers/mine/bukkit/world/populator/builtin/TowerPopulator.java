package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.MagicBlockPopulator;

public class TowerPopulator extends MagicBlockPopulator {
    private List<MaterialAndData> wallBlocks = Collections.emptyList();
    private int minHeight = 32;
    private int maxHeight = 128;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        MagicController controller = world.getController();
        MaterialSet wallSet = controller.getMaterialSetManager().fromConfig(config, "blocks");
        if (wallSet == null) {
            world.getLogger().warning("Invalid block set: " + config.getString("blocks") + ", defaulting to stones");
            wallSet = controller.getMaterialSetManager().getMaterialSet("stones");
        }
        if (wallSet != null) {
            wallBlocks = new ArrayList<>(wallSet.getMaterialsWithData());
        }
        minHeight = config.getInt("min_height", minHeight);
        maxHeight = config.getInt("max_height", maxHeight);
        return true;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int worldHeight = worldInfo.getMaxHeight();
        final int towerHeight = Math.min(worldHeight - 1, RandomUtils.range(random, minHeight, maxHeight));
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = world.getGroundLevel();

        final int buffer = region.getBuffer();
        final int minX = chunkGlobalX - buffer;
        final int maxX = chunkGlobalX + 16 + buffer;
        final int minZ = chunkGlobalZ - buffer;
        final int maxZ = chunkGlobalZ + 16 + buffer;
        final MaterialAndData wallBlock = RandomUtils.getRandom(wallBlocks, random);
        final BlockData wallBlockData = wallBlock.createBlockData();
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = floorLevel + 1; y < towerHeight; y++) {
                    region.setBlockData(x, y, z, wallBlockData);
                }
            }
        }
    }
}
