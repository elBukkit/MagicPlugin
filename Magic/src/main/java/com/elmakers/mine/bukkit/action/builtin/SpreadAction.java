package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;

public class SpreadAction extends CompoundAction
{
    private int radius;
    private float centerProbability;
    private float outerProbability;
    private float yawMax;
    private float pitchMax;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        radius = parameters.getInt("radius", 2);
        yawMax = (float)parameters.getDouble("yaw_max", 0);
        pitchMax = (float)parameters.getDouble("pitch_max", 0);
        centerProbability = (float)parameters.getDouble("probability", 1);
        outerProbability = (float)parameters.getDouble("probability", 1);
        centerProbability = (float)parameters.getDouble("center_probability", centerProbability);
        outerProbability = (float)parameters.getDouble("outer_probability", outerProbability);
    }

    @Override
    public SpellResult step(CastContext context) {
        Location sourceLocation = context.getCastLocation();
        Entity source = context.getEntity();
        Random random = context.getRandom();
        if (pitchMax > 0 || yawMax > 0)
        {
            sourceLocation = sourceLocation.clone();
            if (pitchMax > 0)
            {
                sourceLocation.setPitch(sourceLocation.getPitch() + pitchMax * random.nextFloat() - pitchMax / 2);
            }
            if (yawMax > 0)
            {
                sourceLocation.setYaw(sourceLocation.getYaw() + yawMax * random.nextFloat() - yawMax / 2);
            }
        }
        createActionContext(context, source, sourceLocation);
        Location targetLocation = actionContext.getTargetLocation();
        if (targetLocation != null)
        {
            targetLocation = targetLocation.clone();
            double weight = RandomUtils.lerp(centerProbability - outerProbability, centerProbability + outerProbability, random.nextFloat());
            double xOffset = radius * weight;
            xOffset = xOffset - radius;
            weight = RandomUtils.lerp(centerProbability - outerProbability, centerProbability + outerProbability, random.nextFloat());
            double zOffset = radius * weight;
            zOffset = zOffset - radius;
            targetLocation.setX(targetLocation.getX() + xOffset);
            targetLocation.setZ(targetLocation.getZ() + zOffset);
            actionContext.setTargetLocation(targetLocation);
        }
        return startActions();
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("radius");
        parameters.add("probability");
        parameters.add("center_probability");
        parameters.add("outer_probability");
        parameters.add("yaw_max");
        parameters.add("pitch_max");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("radius")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("probability") || parameterKey.equals("center_probability") || parameterKey.equals("outer_probability")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_PERCENTAGES));
        } else if (parameterKey.equals("yaw_max") || parameterKey.equals("pitch_max")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_VECTOR_COMPONENTS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
