package com.elmakers.mine.bukkit.magic.action.builtin;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.SpellAction;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;

public class PotionEffectAction extends SpellAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters, Entity entity)
	{
		if (!(entity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}

        Integer duration = null;
        if (parameters.contains("duration"))
        {
            duration = parameters.getInt("duration");
        }
        LivingEntity targetEntity = (LivingEntity)entity;
        registerPotionEffects(targetEntity);
		Collection<PotionEffect> effects = spell.getPotionEffects(parameters, duration);
        CompatibilityUtils.applyPotionEffects(targetEntity, effects);

        if (parameters.contains("remove_effects")) {
            List<String> removeKeys = parameters.getStringList("remove_effects");
            for (String removeKey : removeKeys) {
                PotionEffectType removeType = PotionEffectType.getByName(removeKey);
                targetEntity.removePotionEffect(removeType);
            }
        }

		return SpellResult.CAST;
	}
}
