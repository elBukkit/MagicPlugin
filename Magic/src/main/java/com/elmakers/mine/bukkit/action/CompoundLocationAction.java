package com.elmakers.mine.bukkit.action;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public abstract class CompoundLocationAction extends CompoundAction {
    private List<Location> locations = new ArrayList<>();
    private int currentLocation = 0;

    public abstract void addLocations(CastContext context, List<Location> locations);

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        currentLocation = 0;
    }

    @Override
    public SpellResult start(CastContext context) {
        locations.clear();
        addLocations(context, locations);
        context.addWork(10 + locations.size());
        return SpellResult.NO_TARGET;
    }

    @Override
    public boolean next(CastContext context) {
        currentLocation++;
        return currentLocation < locations.size();
    }

    @Override
    public SpellResult step(CastContext context) {
        if (currentLocation < locations.size()) {
            Location location = locations.get(currentLocation);
            actionContext.setTargetLocation(location);
            return startActions();
        }

        return SpellResult.NO_ACTION;
    }

    @Nullable
    @Override
    public Object clone() {
        CompoundLocationAction action = (CompoundLocationAction)super.clone();
        if (action != null) {
            action.locations = new ArrayList<>(this.locations);
        }
        return action;
    }
}
