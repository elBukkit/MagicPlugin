package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.effects.EffectRing;
import com.elmakers.mine.bukkit.effects.EffectTrail;
import com.elmakers.mine.bukkit.effects.ParticleType;
import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.utilities.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class PotionEffectSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		LivingEntity targetEntity = null;
		String targetType = (String)parameters.getString("target", "other");
		
		if (!targetType.equals("self")) {
			this.targetEntity(LivingEntity.class);
			Target target = getTarget();
			if (target != null && target.isEntity() && (target.getEntity() instanceof LivingEntity))
			{
				targetEntity = (LivingEntity)target.getEntity();
			}
		}
		if (targetEntity == null && !targetType.equals("other")) {
			targetEntity = getPlayer();
		} 
		
		if (targetEntity == null)
		{
			return SpellResult.NO_TARGET;
		}
		
		ParticleType particleType = ParticleType.fromName((String)parameters.getString("particle", ""), ParticleType.INSTANT_SPELL);
		if (targetEntity == getPlayer()) {
			Location effectLocation = getPlayer().getEyeLocation();
			EffectRing effect = new EffectRing(controller.getPlugin(), effectLocation, 4, 8);
			effect.setParticleType(particleType);
			effect.setParticleCount(8);
			effect.setEffectData(2);
			effect.setInvert(true);
			effect.start();
		} else {
			
			// Check for superprotected mages
			if (targetEntity instanceof Player) {
				Mage targetMage = controller.getMage((Player)targetEntity);
				
				// Check for protected players
				if (targetMage.isSuperProtected()) {
					return SpellResult.NO_TARGET;
				}
			}
			
			Location effectLocation = getPlayer().getEyeLocation();
			Vector effectDirection = effectLocation.getDirection();
			EffectTrail effect = new EffectTrail(controller.getPlugin(), effectLocation, effectDirection, 32);
			effect.setParticleType(particleType);
			effect.setParticleCount(8);
			effect.setEffectData(2);
			effect.setSpeed(3);
			effect.start();
		}
		
		Collection<PotionEffect> effects = getPotionEffects(parameters);
		targetEntity.addPotionEffects(effects);
		return SpellResult.SUCCESS;
	}
}
