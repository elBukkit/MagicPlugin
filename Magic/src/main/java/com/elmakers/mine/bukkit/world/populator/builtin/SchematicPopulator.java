package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.Schematic;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.SearchType;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class SchematicPopulator extends BaseBlockPopulator {
    private String schematicName;
    private Schematic schematic;
    private IntegerRange position;
    private IntegerRange yOffset;
    private SearchType searchType = SearchType.AREA_UP_MIN;
    private boolean fillAir = false;
    // TODO: Make this on by default once the offset context is working better
    private boolean shiftToFit = false;

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
        searchType = ConfigUtils.parseEnum(config, "search", SearchType.AREA_UP_MIN, getLogger());
        fillAir = config.getBoolean("fill_air", fillAir);
        shiftToFit = config.getBoolean("shift_to_fit", shiftToFit);
        return true;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        MagicController controller = getController();
        if (schematic == null) {
            schematic = controller.loadSchematic(this.schematicName, true);
        }
        if (schematic == null || !schematic.isLoaded()) {
            controller.getLogger().warning("Unknown schematic: " + this.schematicName);
            return;
        }

        Vector size = schematic.getSize();
        final BoundingBox bounds = context.getBounds();
        final Vector maxSize = bounds.size();
        final int sizeY = size.getBlockY();
        final int startXBase = position.getRandom(random);
        final int startZBase = position.getRandom(random);
        final int sizeXBase = Math.min(size.getBlockX(), maxSize.getBlockZ());
        final int sizeZBase = Math.min(size.getBlockZ(), maxSize.getBlockX());
        final int startX = shiftToFit ? Math.min(startXBase, bounds.getMax().getBlockX() - sizeXBase) : startXBase;
        final int startZ = shiftToFit ? Math.min(startZBase,  bounds.getMax().getBlockZ() - sizeZBase) : startZBase;
        final int sizeX = shiftToFit ? sizeXBase : Math.min(sizeXBase, bounds.getMax().getBlockX() - startX);
        final int sizeZ = shiftToFit ? sizeZBase : Math.min(sizeZBase, bounds.getMax().getBlockZ() - startZ);
        final int groundLevel = world.getGroundLevel();
        final int startOffsetY = yOffset.getRandom(random);
        final int startY = 1 + startOffsetY + context.searchArea(searchType, startX, groundLevel, startZ, startX + sizeX, startZ + sizeZ);
        final int maxHeight = context.getMaxHeight();
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dz = 0; dz < sizeZ; dz++) {
                final int x = dx + startX;
                final int z = dz + startZ;
                int yOffset = context.continueSearchArea(searchType, x, startY, z, groundLevel);
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
                        context.setBlockData(x, y, z, blockData);
                    }
                }
            }
        }
    }
}
