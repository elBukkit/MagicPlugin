package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class EndPortalPopulator extends BaseBlockPopulator {
    private IntegerRange portalWidth;
    private IntegerRange yOffset;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        portalWidth = IntegerRange.fromConfig(getLogger(), config, "width", 3, 3);
        yOffset = IntegerRange.fromConfig(getLogger(), config, "y_offset", 0, 0);
        return true;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = world.getGroundLevel();
        final int portalWidth = this.portalWidth.getRandom(random);
        final int yOffset = this.yOffset.getRandom(random);
        final int portalLeft = 8 - (int)Math.ceil((double)portalWidth / 2);
        final int portalRight = 8 + (int)Math.floor((double)portalWidth / 2);

        final int minPortalX = chunkGlobalX + portalLeft;
        final int maxPortalX = chunkGlobalX + portalRight;
        final int minPortalZ = chunkGlobalZ + portalLeft;
        final int maxPortalZ = chunkGlobalZ + portalRight;

        for (int x = minPortalX; x < maxPortalX; x++) {
            for (int z = minPortalZ; z < maxPortalZ; z++) {
                region.setType(x, floorLevel + yOffset, z, Material.END_PORTAL);
            }
        }
    }
}
