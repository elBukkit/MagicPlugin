package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class AggroAction extends BaseSpellAction
{
	@Override
	public SpellResult perform(CastContext context)
	{
        Entity target = context.getTargetEntity();
		if (!(target instanceof Creature))
		{
			return SpellResult.NO_TARGET;
		}
		LivingEntity source = context.getLivingEntity();
		if (source == null)
        {
            return SpellResult.NO_TARGET;
        }

        Creature creatureTarget = (Creature)target;
		LivingEntity current = creatureTarget.getTarget();
		if (source == current)
        {
            return SpellResult.NO_ACTION;
        }

        ((Creature) target).setTarget(source);
		return SpellResult.CAST;
	}

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
