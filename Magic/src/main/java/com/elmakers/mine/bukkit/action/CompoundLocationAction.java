package com.elmakers.mine.bukkit.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public abstract class CompoundLocationAction extends CompoundAction {
    private List<Location> locations = new ArrayList<>();
    private int currentLocation = 0;
    protected int targetCount;

    public abstract void addLocations(CastContext context, List<Location> locations);

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        targetCount = parameters.getInt("target_count", -1);
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        currentLocation = 0;
    }

    @Override
    public SpellResult start(CastContext context) {
        locations.clear();
        addLocations(context, locations);
        if (targetCount > 0) {
            Location source = context.getTargetLocation();
            Collections.sort(locations, new Comparator<Location>() {
                @Override
                public int compare(Location o1, Location o2) {
                    double d1 = o1.distanceSquared(source);
                    double d2 = o2.distanceSquared(source);
                    return (int)(d1 - d2);
                }
            });
        }
        context.addWork(10 + locations.size());
        return SpellResult.NO_TARGET;
    }

    @Override
    public boolean next(CastContext context) {
        currentLocation++;
        if (targetCount > 0 && currentLocation >= targetCount) {
            return false;
        }
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

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("target_count");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("target_count")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
