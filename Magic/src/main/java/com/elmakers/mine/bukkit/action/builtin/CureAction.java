package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CureAction extends BaseSpellAction
{
	private final static PotionEffectType[] _negativeEffects =
			{PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM,
					PotionEffectType.HUNGER, PotionEffectType.POISON, PotionEffectType.SLOW,
					PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.WITHER};
	protected final static Set<PotionEffectType> negativeEffects = new HashSet<>(Arrays.asList(_negativeEffects));

	@Override
	public SpellResult perform(CastContext context)
	{
        Entity entity = context.getTargetEntity();
		if (entity == null || !(entity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}

		LivingEntity targetEntity = (LivingEntity)entity;
		Collection<PotionEffect> currentEffects = targetEntity.getActivePotionEffects();
		for (PotionEffect effect : currentEffects)
		{
			if (negativeEffects.contains(effect.getType()))
			{
				context.registerPotionEffects(targetEntity);
				targetEntity.removePotionEffect(effect.getType());
			}
		}
		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable()
	{
		return true;
	}

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
