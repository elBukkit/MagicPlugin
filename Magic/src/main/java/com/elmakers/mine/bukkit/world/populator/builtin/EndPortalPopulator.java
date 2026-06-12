package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class EndPortalPopulator extends BaseBlockPopulator {
    private int minPortalWidth = 3;
    private int maxPortalWidth = 3;
    private int minTunnelHeight = 0;
    private int maxTunnelHeight = 0;
    private boolean gateway = false;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        minPortalWidth = config.getInt("min_portal_width", minPortalWidth);
        maxPortalWidth = config.getInt("max_portal_width", maxPortalWidth);
        minTunnelHeight = config.getInt("min_tunnel_height", minPortalWidth);
        maxTunnelHeight = config.getInt("max_tunnel_height", maxPortalWidth);
        gateway = config.getBoolean("gateway", gateway);
        return true;
    }

    private BlockData getGatewayBlock() {
        BlockData gatewayData = getController().getPlugin().getServer().createBlockData(Material.END_GATEWAY);
        if (gatewayData instanceof EndGateway) {
            EndGateway gateway = (EndGateway)gatewayData;
            gateway.setAge(-Integer.MAX_VALUE);
        }
        return gatewayData;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = world.getGroundLevel();
        final int portalWidth = RandomUtils.range(random, minPortalWidth, maxPortalWidth);
        final int tunnelHeight = RandomUtils.range(random, minTunnelHeight, maxTunnelHeight);
        final int portalLeft = 8 - (int)Math.ceil((double)portalWidth / 2);
        final int portalRight = 8 + (int)Math.floor((double)portalWidth / 2);

        final int buffer = region.getBuffer();
        final int minX = chunkGlobalX - buffer;
        final int maxX = chunkGlobalX + 16 + buffer;
        final int minZ = chunkGlobalZ - buffer;
        final int maxZ = chunkGlobalZ + 16 + buffer;
        final int minExitX = chunkGlobalX + portalLeft;
        final int maxExitX = chunkGlobalX + portalRight;
        final int minExitZ = chunkGlobalZ + portalLeft;
        final int maxExitZ = chunkGlobalZ + portalRight;
        final int maxExitY = floorLevel + tunnelHeight;
        final BlockData exitBlock = gateway ? getGatewayBlock() : null;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = floorLevel + 1; y <= maxExitY; y++) {
                    if (exitBlock != null && x >= minExitX && x <= maxExitX && z >= minExitZ && z <= maxExitZ) {
                        region.setBlockData(x, y, z, exitBlock);
                    } else {
                        region.setType(x, y, z, Material.AIR);
                    }
                }

                if (x >= minExitX && x <= maxExitX && z >= minExitZ && z <= maxExitZ) {
                    region.setType(x, floorLevel, z, Material.END_PORTAL);
                }
            }
        }
    }
}
