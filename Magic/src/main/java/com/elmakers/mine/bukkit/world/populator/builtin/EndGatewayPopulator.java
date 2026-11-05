package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.SearchType;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class EndGatewayPopulator extends BaseBlockPopulator {
    private IntegerRange gatewaySize;
    private IntegerRange gatewayHeight;
    private IntegerRange position;
    private SearchType searchType = SearchType.AREA_UP_MIN;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        gatewaySize = IntegerRange.fromConfig(getLogger(), config, "size", 3, 3);
        gatewayHeight = IntegerRange.fromConfig(getLogger(), config, "height", 2, 4);
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
        final Random random = context.getRandom();

        final int positionX = position.getRandom(random);
        final int positionZ = position.getRandom(random);
        final int floorLevel = world.getGroundLevel();
        final int gatewayWidth = this.gatewaySize.getRandom(random);
        final int gatewayHeight = this.gatewayHeight.getRandom(random);
        final int portalLeftX = positionX - (gatewayWidth / 2);
        final int portalRightX = portalLeftX + gatewayWidth;
        final int portalLeftZ = positionZ - (gatewayWidth / 2);
        final int portalRightZ = portalLeftZ + gatewayWidth;
        final int minExitX = portalLeftX;
        final int maxExitX = portalRightX;
        final int minExitZ = portalLeftZ;
        final int maxExitZ = portalRightZ;
        final BlockData gatewayBlock = getGatewayBlock();
        final int startY = context.searchArea(searchType, minExitX, floorLevel, minExitZ, maxExitX, maxExitZ);

        for (int x = minExitX; x < maxExitX; x++) {
            for (int z = minExitZ; z < maxExitZ; z++) {
                final int groundY = context.continueSearchArea(searchType, x, startY, z, floorLevel);
                final int maxExitY = Math.min(context.getMaxHeight() - 1, groundY + gatewayHeight);
                for (int y = groundY + 1; y <= maxExitY; y++) {
                    context.setBlockData(x, y, z, gatewayBlock);
                }
            }
        }
    }
}
