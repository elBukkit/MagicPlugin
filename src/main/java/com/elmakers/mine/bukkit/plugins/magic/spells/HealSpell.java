package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class HealSpell extends Spell 
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();
		Entity targetEntity = target.getEntity();
		if (targetEntity != null && targetEntity instanceof LivingEntity)
		{
			castMessage("You heal your target");
			((LivingEntity)targetEntity).setHealth(20);
			return SpellResult.SUCCESS;    
		}
		castMessage("You heal yourself");
		player.setHealth(20);
		player.setExhaustion(0);
		player.setFoodLevel(20);
		return SpellResult.SUCCESS;
	}
}
