package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class EndGatewayPopulator extends BaseBlockPopulator {
    private IntegerRange gatewayWidth;
    private IntegerRange gatewayHeight;
    private String targetWorld;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        gatewayWidth = IntegerRange.fromConfig(getLogger(), config, "gateway_width", 3, 3);
        gatewayHeight = IntegerRange.fromConfig(getLogger(), config, "gateway_height", 2, 4);
        targetWorld = config.getString("target_world", targetWorld);
        return gatewayWidth.getMax() > 0;
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
        final int gatewayWidth = this.gatewayWidth.getRandom(random);
        final int gatewayHeight = this.gatewayHeight.getRandom(random);
        final int portalLeft = 8 - (int)Math.ceil((double)gatewayWidth / 2);
        final int portalRight = 8 + (int)Math.floor((double)gatewayWidth / 2);

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
