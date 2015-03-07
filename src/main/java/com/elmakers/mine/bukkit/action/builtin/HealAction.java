package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.Collection;

public class HealAction extends BaseSpellAction
{
    private double percentage;
    private double amount;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        percentage = parameters.getDouble("percentage", 0);
        amount = parameters.getDouble("amount", 20);
    }

	@Override
	public SpellResult perform(CastContext context)
	{
        Entity entity = context.getEntity();
		if (!(entity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}

        LivingEntity targetEntity = (LivingEntity)entity;
        context.registerModified(targetEntity);
        if (percentage > 0)
        {
            double health = targetEntity.getHealth() + targetEntity.getMaxHealth() * percentage;
            targetEntity.setHealth(Math.min(health, targetEntity.getMaxHealth()));
        }
        else
        {
            Mage mage = context.getMage();
            double health = targetEntity.getHealth() + (amount * mage.getDamageMultiplier());
            targetEntity.setHealth(Math.min(health, targetEntity.getMaxHealth()));
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

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("percentage");
        parameters.add("amount");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("percentage")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_PERCENTAGES)));
        } else if (parameterKey.equals("amount")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
