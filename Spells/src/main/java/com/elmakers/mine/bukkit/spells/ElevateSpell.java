package com.elmakers.mine.bukkit.spells;

import java.util.List;

import org.bukkit.Location;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;

public class ElevateSpell extends Spell
{
    protected boolean ascend()
    {
        Location location = targeting.findPlaceToStand(player.getLocation(), true);
        if (location != null)
        {
            castMessage(player, "You ascend");
            player.teleport(location);
            return true;
        }
        return false;
    }

    protected boolean descend()
    {
        Location location = targeting.findPlaceToStand(player.getLocation(), false);
        if (location != null)
        {
            castMessage(player, "You descend");
            player.teleport(location);
            return true;
        }
        return false;
    }

    @Override
    public String getDescription()
    {
        return "Take the player up or down";
    }

    @Override
    public String getName()
    {
        return "elevate";
    }

    @Override
    public boolean onCast(List<ParameterData> parameters)
    {
        boolean ascend = true;
        for (ParameterData parameter : parameters)
        {
            if (parameter.isFlag("descend"))
            {
                ascend = false;
            }
        }
        if (ascend)
        {
            if (ascend())
            {
                return true;
            }
            castMessage(player, "Nowhere to go up");

        }
        else
        {
            if (descend())
            {
                return true;
            }
            castMessage(player, "Nowhere to go down");
        }

        return false;
    }

}
