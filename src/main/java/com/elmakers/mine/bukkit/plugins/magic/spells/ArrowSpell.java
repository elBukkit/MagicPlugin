package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
		    // Hackily make this an infinite arrow
		    try {
		    	Method getHandleMethod = arrow.getClass().getMethod("getHandle");
		    	Object handle = getHandleMethod.invoke(arrow);
		    	Field fromPlayerField = handle.getClass().getField("fromPlayer");
		    	fromPlayerField.setInt(handle, 2);
		    } catch (Throwable ex) {
		    	ex.printStackTrace();
		    }
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
