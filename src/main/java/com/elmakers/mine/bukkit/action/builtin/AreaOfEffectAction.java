package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.action.CompoundAction;
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

public class AreaOfEffectAction extends CompoundAction
{
    private int radius;
    private boolean targetSelf;
    private int targetCount;
    private List<WeakReference<Entity>> entities = new ArrayList<WeakReference<Entity>>();
    private int currentEntity = 0;
    private CastContext actionContext;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        radius = parameters.getInt("radius", 8);
        targetSelf = parameters.getBoolean("target_self", false);
        targetCount = parameters.getInt("target_count", -1);

        Mage mage = context.getMage();
        if (mage != null)
        {
            radius = (int)(mage.getRadiusMultiplier() * radius);
        }

        entities.clear();
        Entity sourceEntity = context.getEntity();
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
                if ((targetSelf || entity != sourceEntity) && context.canTarget(entity))
                {
                    entities.add(new WeakReference<Entity>(entity));
                }
            }
        }

    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        actionContext = createContext(context);
        currentEntity = 0;
    }

	@Override
    public SpellResult perform(CastContext context)
	{
        SpellResult result = SpellResult.NO_TARGET;
        while (currentEntity < entities.size())
        {
            Entity entity = entities.get(currentEntity).get();
            if (entity == null)
            {
                skippedActions(context);
                continue;
            }
            actionContext.setTargetEntity(entity);
            actionContext.setTargetLocation(entity.getLocation());
            SpellResult entityResult = performActions(actionContext);
            result = result.min(entityResult);
            if (entityResult == SpellResult.PENDING) {
                break;
            }
            currentEntity++;
            if (currentEntity < entities.size())
            {
                super.reset(context);
            }
        }

		return result;
	}

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("radius");
        parameters.add("target_count");
        parameters.add("target_self");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("target_self")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("target_count") || parameterKey.equals("radius")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public Object clone()
    {
        AreaOfEffectAction action = (AreaOfEffectAction)super.clone();
        if (action != null) {
            action.entities = new ArrayList<WeakReference<Entity>>(this.entities);
        }
        return action;
    }
}
