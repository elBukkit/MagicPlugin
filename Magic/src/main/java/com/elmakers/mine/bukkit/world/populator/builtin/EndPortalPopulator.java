package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.world.populator.BaseBlockPopulator;
import com.elmakers.mine.bukkit.world.populator.PopulatorContext;

public class EndPortalPopulator extends BaseBlockPopulator {
    private IntegerRange portalSize;
    private IntegerRange position;

    @Override
    public boolean onLoad(ConfigurationSection config) {
        portalSize = IntegerRange.fromConfig(getLogger(), config, "size", 3, 3);
        position = IntegerRange.fromConfig(getLogger(), config, "position", 8, 8);
        return true;
    }

    @Override
    protected void populate(PopulatorContext context) {
        final Random random = context.getRandom();

        final int positionX = position.getRandom(random);
        final int positionZ = position.getRandom(random);
        final int floorLevel = world.getGroundLevel();
        final int portalWidth = this.portalSize.getRandom(random);
        final int portalLeftX = positionX - (portalWidth / 2);
        final int portalRightX = portalLeftX + portalWidth;
        final int portalLeftZ = positionZ - (portalWidth / 2);
        final int portalRightZ = portalLeftZ + portalWidth;
        final int minPortalX = portalLeftX;
        final int maxPortalX = portalRightX;
        final int minPortalZ = portalLeftZ;
        final int maxPortalZ = portalRightZ;

        for (int x = minPortalX; x < maxPortalX; x++) {
            for (int z = minPortalZ; z < maxPortalZ; z++) {
                context.setType(x, floorLevel, z, Material.END_PORTAL);
            }
        }
    }
}
