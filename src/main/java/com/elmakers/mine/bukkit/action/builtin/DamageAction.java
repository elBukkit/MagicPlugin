package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
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

		double damage = parameters.getDouble("damage", 1);

        LivingEntity targetEntity = (LivingEntity)entity;
        registerModified(targetEntity);
		Mage mage = getMage();
		MageController controller = getController();

		if (controller.isElemental(entity)) {
			damage = parameters.getDouble("elemental_damage", damage);
			controller.damageElemental(entity, damage, 0, mage.getCommandSender());
		} else {
			if (targetEntity instanceof Player) {
				damage = parameters.getDouble("player_damage", damage);
			} else {
				damage = parameters.getDouble("entity_damage", damage);
			}
			CompatibilityUtils.magicDamage(targetEntity, damage * mage.getDamageMultiplier(), mage.getEntity());
		}

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
		parameters.add("player_damage");
		parameters.add("entity_damage");
		parameters.add("elemental_damage");
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey) {
		if (parameterKey.equals("damage") || parameterKey.equals("player_damage")
			|| parameterKey.equals("entity_damage") || parameterKey.equals("elemental_damage")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
		} else {
			super.getParameterOptions(examples, parameterKey);
		}
	}
}
