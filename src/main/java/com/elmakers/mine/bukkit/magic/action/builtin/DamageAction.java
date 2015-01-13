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

public class DamageAction extends SpellAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters, Entity entity)
	{
		if (!(entity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}

        LivingEntity targetEntity = (LivingEntity)entity;
        double damage = parameters.getDouble("damage", 1);
        registerModified(targetEntity);
        CompatibilityUtils.magicDamage(targetEntity, damage * mage.getDamageMultiplier(), mage.getEntity());

		return SpellResult.CAST;
	}
}
