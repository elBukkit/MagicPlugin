package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class TunnelPopulator extends BaseBlockPopulator {
    private IntegerRange tunnelWidth;
    private IntegerRange tunnelHeight;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        tunnelWidth = IntegerRange.fromConfig(getLogger(), config, "tunnel_width", 3, 3);
        tunnelHeight = IntegerRange.fromConfig(getLogger(), config, "tunnel_height", 2, 4);
        return true;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = world.getGroundLevel();
        final int tunnelWidth = this.tunnelWidth.getRandom(random);
        final int tunnelHeight = this.tunnelHeight.getRandom(random);
        final int tunnelLeft = 8 - (int)Math.ceil((double)tunnelWidth / 2);
        final int tunnelRight = 8 + (int)Math.floor((double)tunnelWidth / 2);

        final int buffer = region.getBuffer();
        final int minX = chunkGlobalX - buffer;
        final int maxX = chunkGlobalX + 16 + buffer;
        final int minZ = chunkGlobalZ - buffer;
        final int maxZ = chunkGlobalZ + 16 + buffer;
        final int minExitX = chunkGlobalX + tunnelLeft;
        final int maxExitX = chunkGlobalX + tunnelRight;
        final int minExitZ = chunkGlobalZ + tunnelLeft;
        final int maxExitZ = chunkGlobalZ + tunnelRight;
        final int maxExitY = floorLevel + tunnelHeight;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = floorLevel + 1; y <= maxExitY; y++) {
                    if ((x >= minExitX && x <= maxExitX) || (z >= minExitZ && z <= maxExitZ)) {
                        region.setType(x, y, z, Material.AIR);
                    }
                }
            }
        }
    }
}
