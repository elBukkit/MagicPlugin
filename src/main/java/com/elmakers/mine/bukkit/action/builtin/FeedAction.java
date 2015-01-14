package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class FeedAction extends BaseSpellAction implements EntityAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters, Entity targetEntity)
	{
		if (!(targetEntity instanceof Player))
		{
			return SpellResult.NO_TARGET;
		}

        Player player = (Player)targetEntity;
        player.setExhaustion(0);
        player.setFoodLevel(20);

		return SpellResult.CAST;
	}
}
