package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class EndPortalPopulator extends BaseBlockPopulator {
    private IntegerRange portalWidth;
    private IntegerRange portalDepth;
    private String targetWorld;
    private Material borderMaterial = Material.BEDROCK;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        portalWidth = IntegerRange.fromConfig(getLogger(), config, "width", 3, 3);
        portalDepth = IntegerRange.fromConfig(getLogger(), config, "depth", 0, 0);
        targetWorld = config.getString("target_world", targetWorld);
        return true;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = world.getGroundLevel();
        final int portalWidth = this.portalWidth.getRandom(random);
        final int portalDepth = this.portalDepth.getRandom(random);
        final int portalLeft = 8 - (int)Math.ceil((double)portalWidth / 2);
        final int portalRight = 8 + (int)Math.floor((double)portalWidth / 2);

        final int minExitX = chunkGlobalX + portalLeft;
        final int maxExitX = chunkGlobalX + portalRight;
        final int minExitZ = chunkGlobalZ + portalLeft;
        final int maxExitZ = chunkGlobalZ + portalRight;

        for (int x = minExitX - 1; x <= maxExitX; x++) {
            for (int z = minExitZ - 1; z <= maxExitZ; z++) {
                if (z == minExitZ - 1 || z == maxExitZ || x == minExitX - 1 || x == maxExitX) {
                    for (int y = floorLevel - portalDepth + 1; y < floorLevel; y++) {
                        region.setType(x, y, z, borderMaterial);
                    }
                    continue;
                }
                for (int y = floorLevel - portalDepth + 1; y <= floorLevel; y++) {
                    region.setType(x, y, z, Material.AIR);
                }
                region.setType(x, floorLevel - portalDepth, z, Material.END_PORTAL);
                region.setType(x, floorLevel - portalDepth - 1, z, borderMaterial);
            }
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        return targetWorld;
    }
}
