package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class UndoSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		if (parameters.containsKey("player"))
		{
			String undoPlayer = parameters.getString("player");
			boolean undone = spells.undo(undoPlayer);
			if (undone)
			{
				castMessage("You revert " + undoPlayer + "'s construction");
			}
			else
			{
				castMessage("There is nothing to undo for " + undoPlayer);
			}
			return undone ? SpellResult.SUCCESS : SpellResult.FAILURE;
		}

		if (parameters.containsKey("type"))
		{
			String typeString = (String)parameters.getString("type");
			if (typeString.equals("commit"))
			{
				if (spells.commit(player.getName())) {
					castMessage("Undo queue cleared");
					return SpellResult.SUCCESS;
				} else {
					castMessage("Nothing to commit");
					return SpellResult.FAILURE;
				}
			}
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
						castMessage("You revert your construction");
						return SpellResult.SUCCESS;
					}
				}
				return SpellResult.NO_TARGET;
			}
		}

		/*
		 * No target, or target isn't yours- just undo last
		 */
		boolean undone = spells.undo(player.getName());
		if (undone)
		{
			castMessage("You revert your construction");
		}
		else
		{
			castMessage("Nothing to undo");
		}
		return undone ? SpellResult.SUCCESS : SpellResult.FAILURE;	
	}
}
