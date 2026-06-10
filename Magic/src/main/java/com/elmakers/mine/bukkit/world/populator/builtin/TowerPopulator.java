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
    private int minWidth = 24;
    private int maxWidth = 48;
    private double minTaper = 0;
    private double maxTaper = 0;
    private double minNoise = 0;
    private double maxNoise = 0;

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
        minWidth = config.getInt("min_width", minWidth);
        maxWidth = config.getInt("max_width", maxWidth);
        minNoise = config.getDouble("min_noise", minNoise);
        maxNoise = config.getDouble("max_noise", maxNoise);
        minTaper = config.getDouble("min_taper", minTaper);
        maxTaper = config.getDouble("max_taper", maxTaper);
        return true;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int worldHeight = worldInfo.getMaxHeight();
        final int towerHeight = Math.min(worldHeight - 1, RandomUtils.range(random, minHeight, maxHeight));
        final double taper = RandomUtils.range(random, minTaper, maxTaper);
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = world.getGroundLevel();

        final int buffer = region.getBuffer();
        final int maxWidth = 16 + buffer * 2;
        final int towerWidth = Math.min(maxWidth, RandomUtils.range(random, minWidth, maxWidth));
        final MaterialAndData wallBlock = RandomUtils.getRandom(wallBlocks, random);
        final BlockData wallBlockData = wallBlock.createBlockData();
        final int minY = floorLevel + 1;
        for (int y = minY; y < towerHeight; y++) {
            final double taperFactor = (double)y / (double)(towerHeight - minY);
            final double taperedWidth = (towerWidth - taperFactor * towerWidth) / 2;
            final int towerWidthLeft = (int)Math.floor(taperedWidth);
            final int towerWidthRight = (int)Math.ceil(taperedWidth);
            final int minX = chunkGlobalX + 8 - towerWidthLeft;
            final int maxX = chunkGlobalX + 8 + towerWidthRight;
            final int minZ = chunkGlobalZ + 8 - towerWidthLeft;
            final int maxZ = chunkGlobalZ + 8 + towerWidthRight;
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    region.setBlockData(x, y, z, wallBlockData);
                }
            }
        }
    }
}
