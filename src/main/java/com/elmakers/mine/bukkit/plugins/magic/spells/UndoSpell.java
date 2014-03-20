package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class UndoSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();
		Player player = getPlayer();
		if (target.isEntity() && target.getEntity() instanceof Player)
		{
			// Don't let just anyone rewind someone else's thing
			if (player != null && target.getEntity() != player && !mage.isSuperPowered()) {
				return SpellResult.NO_TARGET;
			}
			
			Mage mage = controller.getMage((Player)target.getEntity());
			return mage.undo() ? SpellResult.CAST : SpellResult.FAIL;
		}
		
		if (target.isBlock())
		{
			boolean targetAll = mage.isSuperPowered();
			boolean undone = false;
			if (targetAll)
			{
				String playerName = controller.undoAny(target.getBlock());
				if (playerName != null) 
				{
					undone = true;
					setTargetName(mage.getName());
				}
			}
			else
			{
				undone = mage.undo(target.getBlock());
			}

			if (undone)
			{
				return SpellResult.CAST;
			}
		}
		
		return SpellResult.NO_TARGET;	
	}
}
