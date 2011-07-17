package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.Spell;

public class UndoSpell extends Spell
{
	@Override
	public boolean onCast(ConfigurationNode parameters) 
	{
	    if (parameters.containsKey("player"))
	    {
	        String undoPlayer = (String)parameters.get("player");
            boolean undone = spells.undo(undoPlayer);
            if (undone)
            {
                castMessage(player, "You revert " + undoPlayer + "'s construction");
            }
            else
            {
                castMessage(player, "There is nothing to undo for " + undoPlayer);
            }
            return undone;
	    }
	    
	    if (parameters.containsKey("type"))
	    {
	        String typeString = (String)parameters.get("type");
	        boolean targetAll = typeString.equals("target_all");
	        if (typeString.equals("target") || targetAll)
	        {
	            targetThrough(Material.GLASS);
                Block target = getTargetBlock();
                if (target != null)
                {
                    boolean undone = false;
                    if (targetAll)
                    {
                        undone = spells.undoAny(player, target);
                    }
                    else
                    {
                        undone = spells.undo(player.getName(), target);
                    }
                    
                    if (undone)
                    {
                        castMessage(player, "You revert your construction");
                        return true;
                    }
                }
                return false;
	        }
	    }
		
		/*
		 * No target, or target isn't yours- just undo last
		 */
		boolean undone = spells.undo(player.getName());
		if (undone)
		{
			castMessage(player, "You revert your construction");
		}
		else
		{
			castMessage(player, "Nothing to undo");
		}
		return undone;	
	}
}
