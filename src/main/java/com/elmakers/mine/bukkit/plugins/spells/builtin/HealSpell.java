package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.Target;

public class HealSpell extends Spell 
{
    public HealSpell()
    {
        setCooldown(5000);
    }
    
	@Override
	public boolean onCast(String[] parameters) 
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

	@Override
	public String getName() 
	{
		return "heal";
	}

	@Override
	public String getDescription() 
	{
		return "Heal yourself";
	}

	@Override
	public String getCategory() 
	{
		return "help";
	}

	@Override
	public Material getMaterial()
	{
		return Material.BREAD;
	}
}
