package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class KillAction extends BaseSpellAction
{
	@Override
	public SpellResult perform(CastContext context)
	{
        Entity entity = context.getTargetEntity();
		if (!(entity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}

        LivingEntity targetEntity = (LivingEntity)entity;
        // Overkill to bypass protection
        if (!targetEntity.isDead()) {
			return SpellResult.NO_TARGET;
        }
		context.registerModified(targetEntity);
		targetEntity.damage(targetEntity.getMaxHealth() * 100);
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
