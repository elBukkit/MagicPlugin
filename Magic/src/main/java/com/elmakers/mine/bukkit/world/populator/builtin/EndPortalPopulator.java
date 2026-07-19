package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class EndPortalPopulator extends BaseBlockPopulator {
    private IntegerRange portalWidth;
    private IntegerRange yOffset;
    private IntegerRange position;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        portalWidth = IntegerRange.fromConfig(getLogger(), config, "width", 3, 3);
        yOffset = IntegerRange.fromConfig(getLogger(), config, "y_offset", 0, 0);
        position = IntegerRange.fromConfig(getLogger(), config, "position", 8, 8);
        return true;
    }

    @Override
    protected void populate(PopulatorContext context) {
        // TODO: Use context
        final Random random = context.getRandom();
        final int chunkX = context.getChunkX();
        final int chunkZ = context.getChunkZ();
        final WorldInfo worldInfo = context.getWorldInfo();
        final LimitedRegion region = context.getRegion();

        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int positionX = position.getRandom(random);
        final int positionZ = position.getRandom(random);
        final int floorLevel = world.getGroundLevel();
        final int portalWidth = this.portalWidth.getRandom(random);
        final int yOffset = this.yOffset.getRandom(random);
        final int portalLeftX = positionX - (portalWidth / 2);
        final int portalRightX = portalLeftX + portalWidth;
        final int portalLeftZ = positionZ - (portalWidth / 2);
        final int portalRightZ = portalLeftZ + portalWidth;
        final int minPortalX = chunkGlobalX + portalLeftX;
        final int maxPortalX = chunkGlobalX + portalRightX;
        final int minPortalZ = chunkGlobalZ + portalLeftZ;
        final int maxPortalZ = chunkGlobalZ + portalRightZ;

        for (int x = minPortalX; x < maxPortalX; x++) {
            for (int z = minPortalZ; z < maxPortalZ; z++) {
                region.setType(x, floorLevel + yOffset, z, Material.END_PORTAL);
            }
        }
    }
}
