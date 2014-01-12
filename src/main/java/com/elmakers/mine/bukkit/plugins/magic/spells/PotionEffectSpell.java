package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.EffectRing;
import com.elmakers.mine.bukkit.utilities.EffectTrail;
import com.elmakers.mine.bukkit.utilities.ParticleType;
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
			targetEntity = player;
		} 
		
		if (targetEntity == null)
		{
			return SpellResult.NO_TARGET;
		}
		
		ParticleType particleType = ParticleType.fromName((String)parameters.getString("particle", ""), ParticleType.INSTANT_SPELL);
		if (targetEntity == player) {
			Location effectLocation = player.getEyeLocation();
			EffectRing effect = new EffectRing(spells.getPlugin(), effectLocation, 4, 8);
			effect.setParticleType(particleType);
			effect.setParticleCount(8);
			effect.setEffectData(2);
			effect.setInvert(true);
			effect.start();
		} else {
			Location effectLocation = player.getEyeLocation();
			Vector effectDirection = effectLocation.getDirection();
			EffectTrail effect = new EffectTrail(spells.getPlugin(), effectLocation, effectDirection, 32);
			effect.setParticleType(particleType);
			effect.setParticleCount(8);
			effect.setEffectData(2);
			effect.setSpeed(3);
			effect.start();
		}
		
		List<PotionEffect> effects = new ArrayList<PotionEffect>();
		PotionEffectType[] effectTypes = PotionEffectType.values();
		for (PotionEffectType effectType : effectTypes) {
			// Why is there a null entry in this list? Maybe a 1.7 bug?
			if (effectType == null) continue;
			String typeName = effectType.getName().toLowerCase();
			if (parameters.containsKey(typeName)) {
				String value = parameters.getString(typeName);
				String[] pieces = value.split(",");
				Integer ticks = Integer.parseInt(pieces[0]);
				Integer power = 1;
				if (pieces.length > 0) {
					power = Integer.parseInt(pieces[1]);
				}
				PotionEffect effect = new PotionEffect(effectType, ticks, power, true);
				effects.add(effect);
			}
		}
		targetEntity.addPotionEffects(effects);
		return SpellResult.SUCCESS;
	}
}
