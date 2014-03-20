package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.World;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class WeatherSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		World world = getWorld();
		boolean hasStorm = world.hasStorm();

		if (hasStorm)
		{
			world.setStorm(false);
			world.setThundering(false);
			/*
            boolean hasThunder = world.isThundering();
            if (hasThunder)
            {

            }
            else
            {
                world.setThundering(true);
                castMessage("You anger the storm");
            }
			 */
		}
		else
		{
			world.setStorm(true);
			// This is mainly so we can have different cast messages and effects, but is a bit of a hack.
			return SpellResult.AREA;
		}
		return SpellResult.CAST;
	}

}
