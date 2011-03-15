package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.magic.Spell;

public class UndoSpell extends Spell
{
	@Override
	public boolean onCast(String[] parameters)
	{
		for (int i = 0; i < parameters.length; i++)
		{
			if (parameters[i].equalsIgnoreCase("player") && i < parameters.length - 1)
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
		 * Use target if targeting
		 */
		targetThrough(Material.GLASS);
		Block target = getTargetBlock();
		if (target != null)
		{
			boolean undone = spells.undo(player.getName(), target);
			if (undone)
			{
				castMessage(player, "You revert your construction");
				return true;
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
