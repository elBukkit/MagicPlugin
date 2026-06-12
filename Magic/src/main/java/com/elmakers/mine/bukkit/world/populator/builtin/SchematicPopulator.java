package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class SchematicPopulator extends BaseBlockPopulator {
    private String schematic;
    private int minPosition = 0;
    private int maxPosition = 0;
    private int minY = 0;
    private int maxY = 0;
    private boolean searchUp = true;
    private boolean conform = false;
    private boolean fillAir = false;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        schematic = config.getString("schematic");
        minY = config.getInt("min_y", minY);
        maxY = config.getInt("max_y", maxY);
        minPosition = config.getInt("min_position", minPosition);
        maxPosition = config.getInt("max_position", maxPosition);
        searchUp = config.getBoolean("search_up", searchUp);
        conform = config.getBoolean("conform", conform);
        fillAir = config.getBoolean("fill_air", fillAir);
        return true;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        MagicController controller = getController();
        Schematic schematic = controller.loadSchematic(this.schematic, true);
        if (schematic == null || !schematic.isLoaded()) {
            controller.getLogger().warning("Unknown schematic: " + this.schematic);
            return;
        }
        Vector size = schematic.getSize();
        final int buffer = region.getBuffer();
        final int chunkBaseX = (chunkX << 4) - buffer;
        final int chunkBaseZ = (chunkZ << 4) - buffer;
        final int maxSize = 16 + buffer * 2;
        final int sizeX = Math.min(maxSize, size.getBlockX());
        final int sizeY = size.getBlockY();
        final int sizeZ = Math.min(maxSize, size.getBlockZ());
        final int offsetX = RandomUtils.range(random, minPosition, maxPosition) + buffer;
        final int offsetZ = RandomUtils.range(random, minPosition, maxPosition) + buffer;
        final int startX = Math.min(maxSize - sizeX, offsetX);
        final int startZ = Math.min(maxSize - sizeZ, offsetZ);
        final int groundY = world.getGroundLevel();
        final int startY = (searchUp ? getTopBlock(worldInfo, region, startX + chunkBaseX, groundY, startZ + chunkBaseZ) : groundY) + RandomUtils.range(random, minY, maxY);
        final int maxHeight = worldInfo.getMaxHeight();
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dz = 0; dz < sizeZ; dz++) {
                final int x = dx + startX + chunkBaseX;
                final int z = dz + startZ + chunkBaseZ;
                int yOffset = 0;
                if (conform) {
                    int conformY = getTopBlock(worldInfo, region, x, startY, z);
                    conformY = getBottomBlock(worldInfo, region, x, conformY, z);
                    yOffset = conformY - startY;
                }

                for (int dy = 0; dy < sizeY; dy++) {
                    final int y = dy + startY + yOffset;
                    if (y >= maxHeight) break;

                    final MaterialAndData block = schematic.getBlock(dx, dy, dz);
                    final Material material = block == null ? null : block.getMaterial();
                    if (material != null) {
                        if (material.isAir() && !fillAir) continue;
                        setBlockData(region, x, y, z, block.createBlockData());
                    }
                }
            }
        }
    }
}
