package com.elmakers.mine.bukkit.plugins.magic.spells;

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
			sendMessage("Requires name and message parameters");
			return SpellResult.FAILURE;
		}
		
		if (!controller.sendMail(getCommandSender(), getPlayer().getName(), parameters.getString("name"), parameters.getString("message"))) {
			sendMessage("Mail Not Sent");
			return SpellResult.FAILURE;
		}
		
		sendMessage("Mail Sent!");
		return SpellResult.SUCCESS;
	}
}
