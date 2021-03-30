package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.bukkit.utility.Targeting;

public class RetargetAction extends CompoundAction {
    private Targeting targeting;
    private double range;

    @Override
    public void initialize(Spell spell, ConfigurationSection baseParameters) {
        super.initialize(spell, baseParameters);
        targeting = new Targeting();
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        targeting.start(context.getEyeLocation());
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        targeting.processParameters(parameters);
        range = parameters.getDouble("range", 32);
    }

    @Override
    public SpellResult step(CastContext context) {
        context.addWork(200);
        Entity targetEntity = null;
        Location targetLocation = null;

        CastContext useContext = context;
        if (hasActions()) {
            useContext = actionContext;
        }
        Target target = targeting.target(useContext, range);
        if (target != null && target.isValid()) {
            targetEntity = target.getEntity();
            targetLocation = target.getLocation();
        }
        useContext.setTargetLocation(targetLocation);
        useContext.setTargetEntity(targetEntity);
        return startActions();
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("fov");
        parameters.add("target_count");
        parameters.add("hitbox");
        parameters.add("range");
        parameters.add("close_range");
        parameters.add("close_fov");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("hitbox")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("target_count") || parameterKey.equals("range") || parameterKey.equals("fov")
                || parameterKey.equals("close_range") || parameterKey.equals("close_fov")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
