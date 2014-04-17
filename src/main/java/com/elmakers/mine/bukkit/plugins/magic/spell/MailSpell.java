package com.elmakers.mine.bukkit.plugins.magic.spell;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;

public class MailSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		// TODO: Multi-word messages....
		if (!parameters.contains("name") || !parameters.contains("message"))
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
