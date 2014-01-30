package com.elmakers.mine.bukkit.plugins.magic.spells;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.effects.EffectRing;
import com.elmakers.mine.bukkit.effects.EffectTrail;
import com.elmakers.mine.bukkit.effects.ParticleType;
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
		if (targetEntity != null && targetEntity instanceof LivingEntity)
		{
			Location effectLocation = getPlayer().getEyeLocation();
			Vector effectDirection = effectLocation.getDirection();
			EffectTrail effect = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, 32);
			effect.setParticleType(ParticleType.HEART);
			effect.setParticleCount(2);
			effect.setEffectData(2);
			effect.setSpeed(3);
			effect.start();
			castMessage("You heal your target");
			LivingEntity li = (LivingEntity)targetEntity;
			li.setHealth(li.getMaxHealth());
			if (targetEntity instanceof Player) {
				Player p = (Player)targetEntity;
				p.setExhaustion(0);
				p.setFoodLevel(20);
			}
			return SpellResult.SUCCESS;
		}
		Location effectLocation = getPlayer().getEyeLocation();
		EffectRing effect = new EffectRing(controller.getPlugin(), effectLocation, 4, 8);
		effect.setParticleType(ParticleType.HEART);
		effect.setParticleCount(1);
		effect.setEffectData(2);
		effect.setInvert(true);
		effect.start();
		castMessage("You heal yourself");
		getPlayer().setHealth(getPlayer().getMaxHealth());
		getPlayer().setExhaustion(0);
		getPlayer().setFoodLevel(20);
		return SpellResult.SUCCESS;
	}
}
