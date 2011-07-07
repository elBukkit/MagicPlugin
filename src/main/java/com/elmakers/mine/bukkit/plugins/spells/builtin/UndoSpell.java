package com.elmakers.mine.bukkit.plugins.spells.builtin;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.spells.Spell;

public class UndoSpell extends Spell
{
    public UndoSpell()
    {
        addVariant("erase", Material.LEVER, getCategory(), "Undo your target construction", "target");
    }
    
	@Override
	public boolean onCast(String[] parameters)
	{
		for (int i = 0; i < parameters.length; i++)
		{
		    if (parameters[i].equalsIgnoreCase("target"))
		    {
		        targetThrough(Material.GLASS);
	            Block target = getTargetBlock();
	            if (target != null)
	            {
	                boolean undone = false;
	                if (player.isOp())
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
		    else if (parameters[i].equalsIgnoreCase("player") && i < parameters.length - 1)
			{
				String undoPlayer = parameters[++i];
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

	@Override
	public String getName()
	{
		return "rewind";
	}

	@Override
	public String getCategory()
	{
		return "construction";
	}

	@Override
	public String getDescription()
	{
		return "Undoes your last action";
	}

	@Override
	public Material getMaterial()
	{
		return Material.WATCH;
	}

}
