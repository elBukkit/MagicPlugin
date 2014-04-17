package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utilities.Target;

public class UndoSpell extends TargetingSpell
{
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
		
		if (target.isValid())
		{
			boolean targetAll = mage.isSuperPowered();
			boolean undone = false;
			if (targetAll)
			{
				Mage targetMage = controller.undoAny(target.getBlock());
				if (targetMage != null) 
				{
					undone = true;
					setTargetName(targetMage.getName());
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
