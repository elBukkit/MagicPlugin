package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.LightSource;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class LightSpell extends Spell {

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Block target = getTargetBlock();
		Location targetLocation = player.getLocation();
		if (target == null) {
			target = targetLocation.getBlock();
		} else {
			targetLocation = target.getLocation();
		}
		int lightLevel = parameters.getInt("size", 15);
		LightSource.createLightSource(targetLocation, lightLevel);
		controller.updateBlock(target);
		return SpellResult.SUCCESS;
	}
}
