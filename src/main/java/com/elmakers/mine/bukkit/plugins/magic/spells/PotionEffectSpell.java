package com.elmakers.mine.bukkit.plugins.magic.spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.plugins.magic.Spell;
import com.elmakers.mine.bukkit.plugins.magic.SpellResult;
import com.elmakers.mine.bukkit.plugins.magic.Target;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class PotionEffectSpell extends Spell
{
	@Override
	public SpellResult onCast(ConfigurationNode parameters) 
	{
		LivingEntity targetEntity = player;
		String targetType = (String)parameters.getString("target", "");
		
		if (targetType.equals("other")) {
			targetEntity = null;
		} else if (targetType.equals("self")) {
			targetEntity = player;
		} else {
			this.targetEntity(LivingEntity.class);
			Target target = getTarget();
			if (target != null && target.isEntity() && (target.getEntity() instanceof LivingEntity))
			{
				targetEntity = (LivingEntity)target.getEntity();
			}
		}
		if (targetEntity == null)
		{
			return SpellResult.NO_TARGET;
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
