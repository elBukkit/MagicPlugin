package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class TunnelPopulator extends BaseBlockPopulator {
    private IntegerRange tunnelSize;
    private IntegerRange tunnelHeight;
    private IntegerRange position;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        tunnelSize = IntegerRange.fromConfig(getLogger(), config, "size", 3, 3);
        tunnelHeight = IntegerRange.fromConfig(getLogger(), config, "height", 2, 4);
        position = IntegerRange.fromConfig(getLogger(), config, "position", 8, 7);
        return true;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final int floorLevel = world.getGroundLevel();
        final int tunnelWidth = this.tunnelSize.getRandom(random);
        final int tunnelHeight = this.tunnelHeight.getRandom(random);
        final int center = position.getRandom(random);
        final int tunnelLeft = center - (tunnelWidth / 2);
        final int tunnelRight = tunnelLeft + tunnelWidth;

        final BoundingBox bounds = context.getBounds();
        final int minX = bounds.getMin().getBlockX();
        final int maxX = bounds.getMax().getBlockX();
        final int minZ = bounds.getMin().getBlockZ();
        final int maxZ = bounds.getMax().getBlockZ();
        final int minExitX = tunnelLeft;
        final int maxExitX = tunnelRight;
        final int minExitZ = tunnelLeft;
        final int maxExitZ = tunnelRight;
        final int maxExitY = floorLevel + tunnelHeight;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = floorLevel + 1; y <= maxExitY; y++) {
                    if ((x >= minExitX && x <= maxExitX) || (z >= minExitZ && z <= maxExitZ)) {
                        context.setType(x, y, z, Material.AIR);
                    }
                }
            }
        }
    }
}
