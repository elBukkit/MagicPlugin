package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;

public class DamageAction extends BaseSpellAction implements EntityAction
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
		Mage mage = getMage();
        CompatibilityUtils.magicDamage(targetEntity, damage * mage.getDamageMultiplier(), mage.getEntity());

		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable()
	{
		return true;
	}

	@Override
	public void getParameterNames(Collection<String> parameters) {
		super.getParameterNames(parameters);
		parameters.add("damage");
	}
}
