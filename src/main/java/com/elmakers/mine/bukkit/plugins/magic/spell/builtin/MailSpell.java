package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.plugins.magic.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;

public class MailSpell extends TargetingSpell
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
