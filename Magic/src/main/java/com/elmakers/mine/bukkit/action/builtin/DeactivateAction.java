package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class DeactivateAction extends BaseSpellAction
{
	@Override
	public SpellResult perform(CastContext context)
	{
        Entity targetEntity = context.getTargetEntity();
        MageController controller = context.getController();
        Mage targetMage = targetEntity != null && controller.isMage(targetEntity)
                ? controller.getMage(targetEntity)
                : null;

        if (targetMage == null)
        {
            return SpellResult.NO_TARGET;
        }

        targetMage.deactivateAllSpells(true, false);
		return SpellResult.CAST;
	}

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
