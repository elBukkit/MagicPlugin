package com.elmakers.mine.bukkit.spells;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.ParameterData;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Time extends Spell
{

    @Override
    public String getDescription()
    {
        return "Change the time of day";
    }

    @Override
    public String getName()
    {
        return "time";
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        long targetTime = 0;
        String timeDescription = "day";
        if (parameters.hasFlag("night"))
        {
            targetTime = 13000;
            timeDescription = "night";
        } 
        else 
        {
            ParameterData param = parameters.get("raw");
            if (param != null)
            {
                targetTime = param.getInteger();
                timeDescription = "raw: " + targetTime;
            }
        }
        
        setRelativeTime(targetTime);
        castMessage(player, "Changed time to " + timeDescription);
        
        return true;
    }

}
