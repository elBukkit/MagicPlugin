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
			castMessage("You calm the storm");
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
			castMessage("You stir up a storm");
		}
		return SpellResult.CAST;
	}

}
