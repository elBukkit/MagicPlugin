package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utilities.Target;

public class CommitSpell extends TargetingSpell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		// You should really use /magic commit for this at this point.
		String typeString = parameters.getString("type", "");
		if (typeString.equalsIgnoreCase("all")) {
			return controller.commitAll() ? SpellResult.CAST : SpellResult.FAIL;
		}

		Target target = getTarget();
		if (target.hasEntity() && target.getEntity() instanceof Player)
		{
			Mage mage = controller.getMage((Player)target.getEntity());
			return mage.commit() ? SpellResult.CAST : SpellResult.FAIL;
		}
		
		return mage.commit() ? SpellResult.CAST : SpellResult.FAIL;
	}
}
