package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.SearchType;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class SchematicPopulator extends BaseBlockPopulator {
    private String schematicName;
    private Schematic schematic;
    private IntegerRange position;
    private IntegerRange yOffset;
    private SearchType searchType = SearchType.AREA_UP_MIN;
    private boolean fillAir = false;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        schematic = null;
        schematicName = config.getString("schematic", "");
        if (schematicName.isEmpty()) {
            world.getController().getLogger().warning("Schematic populator missing 'schematic' name");
            return false;
        }
        position = IntegerRange.fromConfig(getLogger(), config, "position", 0, 0);
        yOffset = IntegerRange.fromConfig(getLogger(), config, "y_offset", 0, 0);
        searchType = ConfigUtils.parseEnum(config, "search_type", SearchType.AREA_UP_MIN, getLogger());
        fillAir = config.getBoolean("fill_air", fillAir);
        return true;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        MagicController controller = getController();
        if (schematic == null) {
            schematic = controller.loadSchematic(this.schematicName, true);
        }
        if (schematic == null || !schematic.isLoaded()) {
            controller.getLogger().warning("Unknown schematic: " + this.schematicName);
            return;
        }

        Vector size = schematic.getSize();
        final int buffer = region.getBuffer();
        final int chunkBaseX = (chunkX << 4);
        final int chunkBaseZ = (chunkZ << 4);
        final int maxSize = 16 + buffer * 2;
        final int sizeY = size.getBlockY();
        final int startX = position.getRandom(random);
        final int startZ = position.getRandom(random);
        final int sizeX = Math.min(maxSize, size.getBlockX());
        final int sizeZ = Math.min(maxSize, size.getBlockZ());
        final int groundLevel = world.getGroundLevel();
        final int startOffsetY = yOffset.getRandom(random);
        final int startY = startOffsetY + searchArea(searchType, worldInfo, region, startX + chunkBaseX, groundLevel, startZ + chunkBaseZ, startX + chunkBaseX + sizeX, startZ + chunkBaseZ + sizeZ);
        final int maxHeight = worldInfo.getMaxHeight();
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dz = 0; dz < sizeZ; dz++) {
                final int x = dx + startX + chunkBaseX;
                final int z = dz + startZ + chunkBaseZ;
                int yOffset = continueSearchArea(searchType, worldInfo, region, x, startY, z, groundLevel);
                for (int dy = 0; dy < sizeY; dy++) {
                    final int y = dy + yOffset;
                    if (y >= maxHeight) break;

                    final MaterialAndData block = schematic.getBlock(dx, dy, dz);
                    final Material material = block == null ? null : block.getMaterial();
                    if (material != null) {
                        if (material.isAir() && !fillAir) continue;
                        BlockData blockData = block.createBlockData();
                        if (blockData == null) {
                            // world.getLogger().warning("Failed to create block data for " + material + " from schematic " + schematicName);
                            continue;
                        }
                        setBlockData(region, x, y, z, blockData);
                    }
                }
            }
        }
    }
}
