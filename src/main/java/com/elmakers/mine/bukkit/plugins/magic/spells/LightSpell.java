package com.elmakers.mine.bukkit.plugins.magic.spells;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.LightSource;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class LightSpell extends Spell {

	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		int lightLevel = parameters.getInt("size", 15);
		LightSource.createLightSource(player.getLocation(), lightLevel);
		return SpellResult.SUCCESS;
	}
}
