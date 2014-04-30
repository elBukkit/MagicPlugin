package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class UndoSpell extends TargetingSpell
{
	String targetPlayerName;
	
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		Player player = getPlayer();
		if (target.hasEntity() && target.getEntity() instanceof Player)
		{
			// Don't let just anyone rewind someone else's thing
			if (player != null && target.getEntity() != player && !mage.isSuperPowered()) {
				return SpellResult.NO_TARGET;
			}
			
			Mage mage = controller.getMage((Player)target.getEntity());
			return mage.undo() ? SpellResult.CAST : SpellResult.FAIL;
		}
		
		Block targetBlock = isLookingDown() ? getLocation().getBlock() : target.getBlock();
		if (targetBlock != null)
		{
			boolean targetAll = mage.isSuperPowered();
			boolean undone = false;
			if (targetAll)
			{
				Mage targetMage = controller.undoAny(targetBlock);
				if (targetMage != null) 
				{
					undone = true;
					setTargetName(targetMage.getName());
				}
			}
			else
			{
				setTargetName(mage.getName());
				undone = mage.undo(targetBlock);
			}

			if (undone)
			{
				return SpellResult.CAST;
			}
		}
		
		return SpellResult.NO_TARGET;	
	}
}
