package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;

public class MailSpell extends TargetingSpell
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters)
	{
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

		// TODO: Multi-word messages....
		if (!parameters.contains("name") || !parameters.contains("message"))
		{
			controller.getLogger().warning("Mail requires name and message parameters");
			return SpellResult.FAIL;
		}

		if (!controller.sendMail(mage.getCommandSender(), player.getName(), parameters.getString("name"), parameters.getString("message"))) {
			return SpellResult.FAIL;
		}

		return SpellResult.CAST;
	}
}
