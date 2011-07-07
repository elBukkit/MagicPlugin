package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.Target;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class DisintegrateSpell extends Spell
{
	private int				defaultSearchDistance	= 32;
	
	public DisintegrateSpell()
    {
	    setCooldown(5000);
    }
	
	@Override
	public boolean onCast(String[] parameters)
	{
	    Target target = getTarget();
        if (target == null)
        {
            castMessage(player, "No target");
            return false;
        }
	    if (target.isEntity())
	    {
	        Entity targetEntity = target.getEntity();
	        if (targetEntity instanceof LivingEntity)
	        {
	            LivingEntity li = (LivingEntity)targetEntity;
	            if (li instanceof Player)
	            {
	                li.damage(8);
	            }
	            else
	            {
	                li.damage(100);
	            }
	            castMessage(player, "ZOT!");
	            return true;
	        }
	    }
	    
		if (!target.hasTarget())
		{
			castMessage(player, "No target");
			return false;
		}

		if (defaultSearchDistance > 0 && target.getDistance() > defaultSearchDistance)
		{
			castMessage(player, "Can't blast that far away");
			return false;
		}
		
		Block targetBlock = target.getBlock();
		BlockList disintigrated = new BlockList();
		disintigrated.add(targetBlock);
		
		if (isUnderwater())
		{
		    targetBlock.setType(Material.STATIONARY_WATER);
		}
		else
		{
		    targetBlock.setType(Material.AIR);
		}
		
		spells.addToUndoQueue(player, disintigrated);
		castMessage(player, "ZAP!");
		
		return true;
	}

	@Override
	public String getName()
	{
		return "disintegrate";
	}

	@Override
	public String getCategory()
	{
		return "mining";
	}

	@Override
	public String getDescription()
	{
		return "Destroy the target block";
	}

	@Override
	public Material getMaterial()
	{
		return Material.BONE;
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		defaultSearchDistance = properties.getInteger("spells-disintegrate-search-distance", defaultSearchDistance);
	}
}
