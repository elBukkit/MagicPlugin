package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.World;

import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class WeatherSpell extends Spell
{

    @Override
    public boolean onCast(String[] parameters)
    {
        World world = player.getWorld();
        boolean hasStorm = world.hasStorm();
        boolean hasThunder = world.isThundering();
        if (hasStorm)
        {
            if (hasThunder)
            {
                world.setStorm(false);
                world.setThundering(false);
                castMessage(player, "You calm the storm");
            }
            else
            {
                world.setThundering(true);
                castMessage(player, "You anger the storm");
            }
        }
        else
        {
            world.setStorm(true);
            castMessage(player, "You stir up a storm");
        }
        return true;
    }

    @Override
    public String getName()
    {
        return "weather";
    }

    @Override
    public String getCategory()
    {
        return "farming";
    }

    @Override
    public String getDescription()
    {
        return "Change the weather";
    }

    @Override
    public Material getMaterial()
    {
        return Material.WATER;
    }

}
