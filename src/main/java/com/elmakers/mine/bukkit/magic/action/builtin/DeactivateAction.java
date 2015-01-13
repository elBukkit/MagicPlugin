package com.elmakers.mine.bukkit.magic.action.builtin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.SpellAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class DeactivateAction extends SpellAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters, Entity targetEntity)
	{
        Mage targetMage = controller.isMage(targetEntity) ? controller.getMage(targetEntity) : null;

        if (targetMage == null)
        {
            return SpellResult.NO_TARGET;
        }

        targetMage.deactivateAllSpells(true, false);
		return SpellResult.CAST;
	}
}
