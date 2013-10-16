package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.entity.Arrow;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class ArrowSpell extends Spell
{ 
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
	    int arrowCount = 1;
	    arrowCount = parameters.getInt("count", arrowCount);
		
		for (int ai = 0; ai < arrowCount; ai++)
		{
		    Arrow arrow = player.launchProjectile(Arrow.class);
		    if (arrow == null)
		    {
		        sendMessage(player, "One of your arrows fizzled");
		        return false;
		    }
		    arrow.setShooter(null);
		    arrow.setTicksLived(1150);
		}
	
		castMessage(player, "You fire some magical arrows");
		
		return true;
	}

    @Override
    public void onLoad(ConfigurationNode node)
    {
        disableTargeting();
    }
}
