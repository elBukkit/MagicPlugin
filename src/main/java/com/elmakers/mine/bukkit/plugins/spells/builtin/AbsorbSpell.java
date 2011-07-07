package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class AbsorbSpell extends Spell 
{
	@Override
	public boolean onCast(String[] parameters) 
	{
		if (!isUnderwater())
		{
			noTargetThrough(Material.STATIONARY_WATER);
			noTargetThrough(Material.WATER);
		}
		Block target = getTargetBlock();
		
		if (target == null) 
		{
			castMessage(player, "No target");
			return false;
		}
		int amount = 1;
			
		castMessage(player, "Absorbing some " + target.getType().name().toLowerCase());
			
		return giveMaterial(target.getType(), amount, (short)0 , target.getData());
	}

	@Override
	public String getName() 
	{
		return "absorb";
	}

	@Override
	public String getDescription() 
	{
		return "Give yourself some of your target";
	}

	@Override
	public String getCategory() 
	{
		return "construction";
	}
	
	@Override
	public void onLoad(PluginProperties properties)
	{
		//defaultAmount = properties.getInteger("spells-absorb-amount", defaultAmount);
	}

	@Override
	public Material getMaterial()
	{
		return Material.BUCKET;
	}
}
