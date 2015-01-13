package com.elmakers.mine.bukkit.magic.action.builtin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.SpellAction;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeedAction extends SpellAction
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
