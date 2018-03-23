package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

@Deprecated
public class WeatherSpell extends BaseSpell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters)
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
			return SpellResult.ALTERNATE;
		}
		return SpellResult.CAST;
	}

}
