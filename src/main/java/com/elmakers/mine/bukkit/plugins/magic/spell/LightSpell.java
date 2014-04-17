package com.elmakers.mine.bukkit.plugins.magic.spell;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;

public class LightSpell extends Spell {

	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		sendMessage("This spell has been disabled for safety!");
		return SpellResult.FAIL;
		
		/*
		Block target = getTargetBlock();
		Location targetLocation = getLocation();
		if (target == null) {
			target = targetLocation.getBlock();
		} else {
			targetLocation = target.getLocation();
		}
		int lightLevel = parameters.getInt("size", 15);
		LightSource.createLightSource(targetLocation, lightLevel);
		controller.updateBlock(target);
		return SpellResult.CAST;
		*/
	}
}
