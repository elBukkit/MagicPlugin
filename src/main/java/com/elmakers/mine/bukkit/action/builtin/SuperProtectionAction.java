package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;

public class SuperProtectionAction extends BaseSpellAction
{
	private int duration;

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
		duration = parameters.getInt("duration");
	}

	public SpellResult perform(CastContext context)
	{
        Mage mage = context.getController().getMage(context.getTargetEntity());
		mage.enableSuperProtection(duration);
		return SpellResult.CAST;
	}

    @Override
    public boolean requiresTarget()
    {
        return true;
    }

	@Override
	public boolean requiresTargetEntity()
	{
		return true;
	}
}
