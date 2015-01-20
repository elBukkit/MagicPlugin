package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class FallProtectionAction extends BaseSpellAction implements EntityAction
{
	@Override
	public SpellResult perform(ConfigurationSection parameters, Entity targetEntity)
	{
        int fallProtection = parameters.getInt("duration", 5000);
        MageController controller = getController();
        Mage targetMage = controller.isMage(targetEntity) ? controller.getMage(targetEntity) : null;

        if (targetMage == null)
        {
            return SpellResult.NO_TARGET;
        }

        targetMage.enableFallProtection(fallProtection, getSpell());
		return SpellResult.CAST;
	}
}
