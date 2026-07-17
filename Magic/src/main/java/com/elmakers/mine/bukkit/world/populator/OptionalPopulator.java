package com.elmakers.mine.bukkit.world.populator;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.world.LocationContext;
import com.elmakers.mine.bukkit.world.WorldContext;

public abstract class OptionalPopulator extends BaseBlockPopulator {
    private BaseBlockPopulator populator;
    private int frequency = 2;

    public boolean onLoad(ConfigurationSection config) {
        frequency = config.getInt("frequency", frequency);
        populator = BaseBlockPopulator.parsePopulator(world, config);
        return populator != null;
    }

    protected abstract BaseBlockPopulator getPopulator(WorldContext context);

    @Override
    public void populate(PopulatorContext context) {
        BaseBlockPopulator populator = getPopulator(context);
        if (populator != null) {
            populator.populate(context);
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        String targetWorld = super.getPortalTargetWorld(location);
        if (targetWorld != null) {
            return targetWorld;
        }
        LocationContext context = new LocationContext(location);
        BaseBlockPopulator populator = getPopulator(context);
        if (populator == null) {
            return null;
        }
        return populator.getPortalTargetWorld(location);
    }
}
