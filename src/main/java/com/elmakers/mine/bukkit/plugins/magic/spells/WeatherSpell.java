package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Map;

import org.bukkit.World;

import com.elmakers.mine.bukkit.plugins.magic.Spell;

public class WeatherSpell extends Spell
{
    @Override
    public boolean onCast(Map<String, Object> parameters)
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
