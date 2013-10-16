package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class HealSpell extends Spell 
{
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
        Target target = getTarget();
        Entity targetEntity = target.getEntity();
	    if (targetEntity != null && targetEntity instanceof LivingEntity)
	    {
	        castMessage(player, "You heal your target");
	        ((LivingEntity)targetEntity).setHealth(20);
	        return true;    
	    }
		castMessage(player, "You heal yourself");
		player.setHealth(20);
		player.setExhaustion(0);
		return true;
	}
}
