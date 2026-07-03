package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.SearchType;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;

public class EndGatewayPopulator extends BaseBlockPopulator {
    private IntegerRange gatewayWidth;
    private IntegerRange gatewayHeight;
    private IntegerRange position;
    private IntegerRange yOffset;
    private SearchType searchType = SearchType.AREA_UP_MIN;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        gatewayWidth = IntegerRange.fromConfig(getLogger(), config, "width", 3, 3);
        gatewayHeight = IntegerRange.fromConfig(getLogger(), config, "height", 2, 4);
        yOffset = IntegerRange.fromConfig(getLogger(), config, "y_offset", 0, 0);
        position = IntegerRange.fromConfig(getLogger(), config, "position", 8, 8);
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
        final int center = position.getRandom(random);
        final int yOffset = this.yOffset.getRandom(random);
        final int floorLevel = world.getGroundLevel();
        final int gatewayWidth = this.gatewayWidth.getRandom(random);
        final int gatewayHeight = this.gatewayHeight.getRandom(random);
        final int portalLeft = center - (gatewayWidth / 2);
        final int portalRight = portalLeft + gatewayWidth;
        final int minExitX = chunkGlobalX + portalLeft;
        final int maxExitX = chunkGlobalX + portalRight;
        final int minExitZ = chunkGlobalZ + portalLeft;
        final int maxExitZ = chunkGlobalZ + portalRight;
        final BlockData gatewayBlock = getGatewayBlock();
        final int startY = searchArea(searchType, worldInfo, region, minExitX, floorLevel, minExitZ, maxExitX, maxExitZ);

        for (int x = minExitX; x < maxExitX; x++) {
            for (int z = minExitZ; z < maxExitZ; z++) {
                final int groundY = continueSearchArea(searchType, worldInfo, region, x, startY, z, floorLevel) + yOffset;
                final int maxExitY = Math.min(worldInfo.getMaxHeight() - 1, groundY + gatewayHeight);
                for (int y = groundY + 1; y <= maxExitY; y++) {
                    region.setBlockData(x, y, z, gatewayBlock);
                }
            }
        }
    }
}
