package com.elmakers.mine.bukkit.action.builtin;

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

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        radius = parameters.getInt("radius", 8);
        targetSelf = parameters.getBoolean("target_self", false);
        targetCount = parameters.getInt("target_count", -1);
    }

	@Override
    public SpellResult perform(CastContext context)
	{
		Mage mage = context.getMage();
		Entity sourceEntity = context.getEntity();
		Location sourceLocation = context.getTargetLocation();
        Spell spell = context.getSpell();

        int radius = this.radius;
		if (mage != null)
		{
			radius = (int)(mage.getRadiusMultiplier() * radius);
			sourceEntity = mage.getEntity();
		}

		List<Entity> entities = CompatibilityUtils.getNearbyEntities(sourceLocation, radius, radius, radius);
        SpellResult result = SpellResult.NO_TARGET;
        CastContext actionContext = createContext(context);
		if (targetCount > 0)
		{
			List<Target> targets = new ArrayList<Target>();
			for (Entity entity : entities)
			{
				if ((targetSelf || entity != sourceEntity) && spell.canTarget(entity))
				{
					targets.add(new Target(sourceLocation, entity, radius));
				}
			}
			Collections.sort(targets);
            for (int i = 0; i < targetCount && i < targets.size(); i++)
			{
                Target target = targets.get(i);
                actionContext.setTargetEntity(target.getEntity());
                actionContext.setTargetLocation(target.getLocation());
                SpellResult entityResult = performActions(actionContext);
                result = result.min(entityResult);
			}
		}
		else
		{
			for (Entity entity : entities)
			{
				if ((targetSelf || entity != sourceEntity) && spell.canTarget(entity))
				{
                    actionContext.setTargetEntity(entity);
                    actionContext.setTargetLocation(entity.getLocation());
                    SpellResult entityResult = performActions(actionContext);
                    result = result.min(entityResult);
				}
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
}
