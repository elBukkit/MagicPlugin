package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.Target;

public class HealSpell extends Spell 
{
	@Override
	public boolean onCast(Map<String, Object> parameters) 
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
		return true;
	}
}
