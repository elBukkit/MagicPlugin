package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.World;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class WeatherSpell extends Spell
{
    @Override
    public boolean onCast(ConfigurationNode parameters) 
    {
        World world = player.getWorld();
        boolean hasStorm = world.hasStorm();
        
        if (hasStorm)
        {
            world.setStorm(false);
            world.setThundering(false);
            castMessage(player, "You calm the storm");
            /*
            boolean hasThunder = world.isThundering();
            if (hasThunder)
            {
                
            }
            else
            {
                world.setThundering(true);
                castMessage(player, "You anger the storm");
            }
            */
        }
        else
        {
            world.setStorm(true);
            castMessage(player, "You stir up a storm");
        }
        return true;
    }
}
