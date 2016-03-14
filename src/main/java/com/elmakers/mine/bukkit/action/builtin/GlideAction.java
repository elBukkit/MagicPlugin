package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class GlideAction extends BaseSpellAction
{
	@Override
	public SpellResult perform(CastContext context)
	{
        Entity targetEntity = context.getTargetEntity();
		if (!(targetEntity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}

		LivingEntity livingEntity = (LivingEntity)targetEntity;
		livingEntity.setGliding(true);

		return SpellResult.CAST;
	}

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
