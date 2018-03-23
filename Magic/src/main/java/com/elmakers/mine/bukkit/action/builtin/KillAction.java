package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class KillAction extends BaseSpellAction
{
	@Override
	public SpellResult perform(CastContext context)
	{
        Entity entity = context.getTargetEntity();
		if (!(entity instanceof Damageable))
		{
			return SpellResult.NO_TARGET;
		}

		Damageable targetEntity = (Damageable)entity;
		if (targetEntity.isDead()) {
			return SpellResult.NO_TARGET;
		}
        // Overkill to bypass protection
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
