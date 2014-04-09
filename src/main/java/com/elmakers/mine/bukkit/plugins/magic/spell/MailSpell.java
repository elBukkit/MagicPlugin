package com.elmakers.mine.bukkit.plugins.magic.spell;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class MailSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		// TODO: Multi-word messages....
		if (!parameters.containsKey("name") || !parameters.containsKey("message"))
		{
			controller.getLogger().warning("Mail requires name and message parameters");
			return SpellResult.FAIL;
		}
		
		if (!controller.sendMail(getCommandSender(), getPlayer().getName(), parameters.getString("name"), parameters.getString("message"))) {
			return SpellResult.FAIL;
		}
		
		return SpellResult.CAST;
	}
}
