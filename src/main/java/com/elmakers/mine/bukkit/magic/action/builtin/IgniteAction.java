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

public class IgniteAction extends SpellAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters, Entity entity)
	{
		if (!(entity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}

        int duration = parameters.getInt("duration", 5000);
        int ticks = duration * 20 / 1000;
        LivingEntity targetEntity = (LivingEntity)entity;

        registerModified(targetEntity);
        targetEntity.setFireTicks(ticks);

		return SpellResult.CAST;
	}
}
