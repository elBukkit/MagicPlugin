package com.elmakers.mine.bukkit.world.populator;

import java.util.List;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.world.LocationContext;
import com.elmakers.mine.bukkit.world.WorldContext;

public abstract class CompoundPopulator extends BaseBlockPopulator {

    protected abstract List<BaseBlockPopulator> getPopulators(WorldContext context);

    @Override
    protected void populate(PopulatorContext context) {
        for (BaseBlockPopulator populator : getPopulators(context)) {
            populator.updateAndPopulate(context);
        }
    }

    @Override
    public String getPortalTargetWorld(Location location) {
        String targetWorld = super.getPortalTargetWorld(location);
        if (targetWorld != null) {
            return targetWorld;
        }
        final LocationContext context = new LocationContext(location);
        for (BaseBlockPopulator populator : getPopulators(context)) {
            targetWorld = populator.getPortalTargetWorld(location);
            if (targetWorld != null) {
                return targetWorld;
            }
        }
        return null;
    }
}
