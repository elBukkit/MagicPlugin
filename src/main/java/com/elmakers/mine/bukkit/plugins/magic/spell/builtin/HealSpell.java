package com.elmakers.mine.bukkit.plugins.magic.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.spell.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utilities.Target;

public class HealSpell extends TargetingSpell 
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		Entity targetEntity = target.getEntity();
		Player player = getPlayer();
		if (targetEntity != null && targetEntity instanceof LivingEntity && targetEntity != player)
		{
			LivingEntity li = (LivingEntity)targetEntity;
			li.setHealth(li.getMaxHealth());
			if (targetEntity instanceof Player) {
				Player p = (Player)targetEntity;
				p.setExhaustion(0);
				p.setFoodLevel(20);
			}
			return SpellResult.CAST;
		}
		if (player == null) {
			return SpellResult.NO_TARGET;
		}
		player.setHealth(getPlayer().getMaxHealth());
		player.setExhaustion(0);
		player.setFoodLevel(20);
		return SpellResult.CAST;
	}
}
