package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Map;

import net.minecraft.server.EntityArrow;

import org.bukkit.craftbukkit.entity.CraftArrow;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Arrow;

import com.elmakers.mine.bukkit.plugins.magic.Spell;

public class ArrowSpell extends Spell
{ 
	@Override
	public boolean onCast(Map<String, Object> parameters)
	{
	    int arrowCount = 1;
	    if (parameters.containsKey("count"))
	    {
	        arrowCount = (Integer)parameters.get("count");
	    }
		CraftPlayer cp = (CraftPlayer)player;
		
		for (int ai = 0; ai < arrowCount; ai++)
		{
		    Arrow arrow = cp.shootArrow();
		    if (arrow == null)
		    {
		        sendMessage(player, "One of your arrows fizzled");
		        return false;
		    }
		    if (arrow instanceof CraftArrow)
		    {
                CraftArrow ca = (CraftArrow)arrow;
                EntityArrow ea = (EntityArrow)ca.getHandle();
                
                // Make it so this arrow can't be picked up
                ea.fromPlayer = false;
                
                // Make it so it disappears very quickly after sticking
                // ... inaccessible! >:(
                // ea.j = 1150;
                
    		    if (ai != 0)
    		    {
    		        ea.setPosition
    		        (
    	                ea.locX + Math.random() * arrowCount - arrowCount / 2,
    	                ea.locY + Math.random() * arrowCount - arrowCount / 2,
    	                ea.locZ + Math.random() * arrowCount - arrowCount / 2
    		        );
    		    }
		    }
		}
	
		castMessage(player, "You fire some magical arrows");
		
		return true;
	}
}
