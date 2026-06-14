package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Location;
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
    private int minGatewayHeight = 0;
    private int maxGatewayHeight = 0;
    private String targetWorld;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        minPortalWidth = config.getInt("min_portal_width", minPortalWidth);
        maxPortalWidth = config.getInt("max_portal_width", maxPortalWidth);
        minGatewayHeight = config.getInt("min_gateway_height", minPortalWidth);
        maxGatewayHeight = config.getInt("max_gateway_height", maxPortalWidth);
        targetWorld = config.getString("target_world", targetWorld);
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
        final int gatewayHeight = RandomUtils.range(random, minGatewayHeight, maxGatewayHeight);
        final int portalLeft = 8 - (int)Math.ceil((double)portalWidth / 2);
        final int portalRight = 8 + (int)Math.floor((double)portalWidth / 2);

        final int minExitX = chunkGlobalX + portalLeft;
        final int maxExitX = chunkGlobalX + portalRight;
        final int minExitZ = chunkGlobalZ + portalLeft;
        final int maxExitZ = chunkGlobalZ + portalRight;
        final int maxExitY = floorLevel + gatewayHeight;
        final BlockData exitBlock = gatewayHeight > 0 ? getGatewayBlock() : null;

        for (int x = minExitX; x < maxExitX; x++) {
            for (int z = minExitZ; z < maxExitZ; z++) {
                for (int y = floorLevel + 1; y <= maxExitY && exitBlock != null; y++) {
                    region.setBlockData(x, y, z, exitBlock);
                }
                region.setType(x, floorLevel, z, Material.END_PORTAL);
            }
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        return targetWorld;
    }
}
