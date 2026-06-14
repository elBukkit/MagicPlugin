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

public class EndGatewayPopulator extends BaseBlockPopulator {
    private int minGatewayWidth = 3;
    private int maxGatewayWidth = 3;
    private int minGatewayHeight = 2;
    private int maxGatewayHeight = 4;
    private String targetWorld;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        minGatewayWidth = config.getInt("min_gateway_width", minGatewayWidth);
        maxGatewayWidth = config.getInt("max_gateway_width", maxGatewayWidth);
        minGatewayHeight = config.getInt("min_gateway_height", minGatewayHeight);
        maxGatewayHeight = config.getInt("max_gateway_height", maxGatewayHeight);
        targetWorld = config.getString("target_world", targetWorld);
        return maxGatewayWidth > 0;
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
        final int portalWidth = RandomUtils.range(random, minGatewayWidth, maxGatewayWidth);
        final int gatewayHeight = RandomUtils.range(random, minGatewayHeight, maxGatewayHeight);
        final int portalLeft = 8 - (int)Math.ceil((double)portalWidth / 2);
        final int portalRight = 8 + (int)Math.floor((double)portalWidth / 2);

        final int minExitX = chunkGlobalX + portalLeft;
        final int maxExitX = chunkGlobalX + portalRight;
        final int minExitZ = chunkGlobalZ + portalLeft;
        final int maxExitZ = chunkGlobalZ + portalRight;
        final int maxExitY = floorLevel + gatewayHeight;
        final BlockData gatewayBlock = getGatewayBlock();

        for (int x = minExitX - 1; x <= maxExitX; x++) {
            for (int z = minExitZ - 1; z <= maxExitZ; z++) {
                for (int y = floorLevel + 1; y <= maxExitY; y++) {
                    region.setBlockData(x, y, z, gatewayBlock);
                }
            }
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        return targetWorld;
    }
}
