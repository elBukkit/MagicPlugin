package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;

public class LightSpell extends TargetingSpell {

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
