package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundEntityAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AreaOfEffectAction extends CompoundEntityAction
{
    private int radius;
    private int targetCount;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        createActionContext(context, context.getTargetEntity(), context.getTargetLocation());
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        radius = parameters.getInt("radius", 8);
        targetCount = parameters.getInt("target_count", -1);

        Mage mage = context.getMage();
        if (mage != null)
        {
            radius = (int)(mage.getRadiusMultiplier() * radius);
        }

        super.prepare(context, parameters);
    }

    @Override
    public void addEntities(CastContext context, List<WeakReference<Entity>> entities)
    {
        Entity sourceEntity = context.getMage().getEntity();
        Location sourceLocation = context.getTargetLocation();
        List<Entity> candidates = CompatibilityUtils.getNearbyEntities(sourceLocation, radius, radius, radius);
        if (targetCount > 0)
        {
            List<Target> targets = new ArrayList<Target>();
            for (Entity entity : candidates)
            {
                if ((targetSelf || entity != sourceEntity) && context.canTarget(entity))
                {
                    targets.add(new Target(sourceLocation, entity, radius));
                }
            }
            Collections.sort(targets);
            for (int i = 0; i < targetCount && i < targets.size(); i++)
            {
                Target target = targets.get(i);
                entities.add(new WeakReference<Entity>(target.getEntity()));
            }
        }
        else
        {
            for (Entity entity : candidates)
            {
                if ((targetSelf || !entity.equals(sourceEntity)) && context.canTarget(entity))
                {
                    entities.add(new WeakReference<Entity>(entity));
                }
            }
        }
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("radius");
        parameters.add("target_count");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("target_count") || parameterKey.equals("radius")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
