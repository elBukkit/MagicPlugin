package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class HealSpell extends TargetingSpell 
{
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		Target target = getTarget();
		Entity targetEntity = target.getEntity();
        if (targetEntity == null || !(targetEntity instanceof LivingEntity)) {
            return SpellResult.NO_TARGET;
        }
        LivingEntity li = (LivingEntity)targetEntity;
        li.setHealth(li.getMaxHealth());
        if (targetEntity instanceof Player) {
            Player p = (Player)targetEntity;
            p.setExhaustion(0);
            p.setFoodLevel(20);
        }
        return SpellResult.CAST;
	}
}
