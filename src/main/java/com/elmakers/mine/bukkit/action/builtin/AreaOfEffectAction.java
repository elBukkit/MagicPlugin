package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.CompoundAction;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AreaOfEffectAction extends CompoundAction implements BlockAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters, Block block)
	{
		int radius = parameters.getInt("radius", 8);
		boolean targetSelf = parameters.getBoolean("target_self", false);
		int targetCount = parameters.getInt("target_count", -1);
		Mage mage = getMage();
		Entity sourceEntity = null;
		Spell spell = getSpell();
		Location sourceLocation = block.getLocation();

		if (mage != null)
		{
			radius = (int)(mage.getRadiusMultiplier() * radius);
			sourceEntity = mage.getEntity();
		}

		List<Entity> entities = CompatibilityUtils.getNearbyEntities(sourceLocation, radius, radius, radius);
		List<Entity> targetEntities = new ArrayList<Entity>();

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
			for (Target target : targets)
			{
				if (targetEntities.size() >= targetCount) break;
				targetEntities.add(target.getEntity());
			}
		}
		else
		{
			for (Entity entity : entities)
			{
				if ((targetSelf || entity != sourceEntity) && spell.canTarget(entity))
				{
					targetEntities.add(entity);
				}
			}
		}

		return perform(parameters, block.getLocation(), targetEntities);
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
