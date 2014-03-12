package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class HealSpell extends Spell 
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		Target target = getTarget();
		Entity targetEntity = target.getEntity();
		Player player = getPlayer();
		if (targetEntity != null && targetEntity instanceof LivingEntity && targetEntity != player)
		{
			castMessage("You heal your target");
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
		castMessage("You heal yourself");
		player.setHealth(getPlayer().getMaxHealth());
		player.setExhaustion(0);
		player.setFoodLevel(20);
		return SpellResult.CAST;
	}
}
