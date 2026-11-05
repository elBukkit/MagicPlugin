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
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class EndGatewayPopulator extends BaseBlockPopulator {
    private IntegerRange gatewaySize;
    private IntegerRange gatewayHeight;
    private IntegerRange position;
    private IntegerRange yOffset;
    private SearchType searchType = SearchType.AREA_UP_MIN;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        gatewaySize = IntegerRange.fromConfig(getLogger(), config, "size", 3, 3);
        gatewayHeight = IntegerRange.fromConfig(getLogger(), config, "height", 2, 4);
        yOffset = IntegerRange.fromConfig(getLogger(), config, "y_offset", 0, 0);
        position = IntegerRange.fromConfig(getLogger(), config, "position", 8, 8);
        return gatewaySize.getMax() > 0;
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
        final int yOffset = this.yOffset.getRandom(random);
        final int floorLevel = world.getGroundLevel();
        final int gatewayWidth = this.gatewaySize.getRandom(random);
        final int gatewayHeight = this.gatewayHeight.getRandom(random);
        final int portalLeftX = positionX - (gatewayWidth / 2);
        final int portalRightX = portalLeftX + gatewayWidth;
        final int portalLeftZ = positionZ - (gatewayWidth / 2);
        final int portalRightZ = portalLeftZ + gatewayWidth;
        final int minExitX = chunkGlobalX + portalLeftX;
        final int maxExitX = chunkGlobalX + portalRightX;
        final int minExitZ = chunkGlobalZ + portalLeftZ;
        final int maxExitZ = chunkGlobalZ + portalRightZ;
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
