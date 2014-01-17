package com.elmakers.mine.bukkit.plugins.magic.spells;

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
			boolean undone = controller.undo(undoPlayer);
			if (undone)
			{
				sendMessage("You revert " + undoPlayer + "'s construction");
			}
			else
			{
				sendMessage("There is nothing to undo for " + undoPlayer);
			}
			return undone ? SpellResult.SUCCESS : SpellResult.FAILURE;
		}

		if (parameters.containsKey("type"))
		{
			String typeString = (String)parameters.getString("type");
			if (typeString.equals("commit"))
			{
				if (controller.commit(getPlayer().getName())) {
					sendMessage("Undo queue cleared");
					return SpellResult.SUCCESS;
				} else {
					castMessage("Nothing to commit");
					return SpellResult.FAILURE;
				}
			}
			else if (typeString.equals("commitall"))
			{
				if (controller.commitAll()) {
					sendMessage("All undo queues cleared");
					return SpellResult.SUCCESS;
				} else {
					castMessage("Nothing in any undo queues");
					return SpellResult.FAILURE;
				}
			}
			boolean targetAll = typeString.equals("target_all");
			if (typeString.equals("target") || targetAll)
			{
				// targetThrough(Material.GLASS);
				Block target = getTargetBlock();
				if (target != null)
				{
					boolean undone = false;
					if (targetAll)
					{
						undone = controller.undoAny(getPlayer(), target);
					}
					else
					{
						undone = controller.undo(getPlayer().getName(), target);
						if (undone) {
							sendMessage("You revert your construction");
						}
					}

					if (undone)
					{
						return SpellResult.SUCCESS;
					}
				}
				return SpellResult.NO_TARGET;
			}
		}

		/*
		 * No target, or target isn't yours- just undo last
		 */
		boolean undone = controller.undo(getPlayer().getName());
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
