package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class HealAction extends BaseSpellAction implements EntityAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters, Entity entity)
	{
		if (!(entity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}

        LivingEntity targetEntity = (LivingEntity)entity;
        registerModified(targetEntity);
        if (parameters.contains("percentage"))
        {
            double health = targetEntity.getHealth() + targetEntity.getMaxHealth() * parameters.getDouble("percentage");
            targetEntity.setHealth(Math.min(health, targetEntity.getMaxHealth()));
        }
        else
        {
            double health = targetEntity.getHealth() + parameters.getDouble("amount", 20);
            targetEntity.setHealth(Math.min(health, targetEntity.getMaxHealth()));
        }

		return SpellResult.CAST;
	}

    @Override
    public boolean isUndoable()
    {
        return true;
    }
}
