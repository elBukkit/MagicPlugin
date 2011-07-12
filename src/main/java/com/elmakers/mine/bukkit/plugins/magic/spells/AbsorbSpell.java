package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.PluginProperties;

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
	public void onLoad(PluginProperties properties)
	{
		//defaultAmount = properties.getInteger("spells-absorb-amount", defaultAmount);
	}
}
