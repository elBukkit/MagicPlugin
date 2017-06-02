package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.Collection;

public class GlideAction extends BaseSpellAction
{
	private boolean waitForLanding;
	private boolean isGliding = false;

	@Override
	public SpellResult perform(CastContext context)
	{
        Entity targetEntity = context.getTargetEntity();
		if (!(targetEntity instanceof LivingEntity))
		{
			return SpellResult.NO_TARGET;
		}

		LivingEntity livingEntity = (LivingEntity)targetEntity;

		if (isGliding) {
			if (!livingEntity.isGliding()) {
				isGliding = false;
				return SpellResult.CAST;
			}
		} else {
			livingEntity.setGliding(true);
			isGliding = true;
		}

		return waitForLanding ? SpellResult.PENDING : SpellResult.CAST;
	}

	@Override
	public void getParameterNames(Spell spell, Collection<String> parameters)
	{
		super.getParameterNames(spell, parameters);
		parameters.add("wait_for_landing");
	}

	@Override
	public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
	{
		if (parameterKey.equals("wait_for_landing")) {
			examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
		} else {
			super.getParameterOptions(spell, parameterKey, examples);
		}
	}

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters)
	{
		waitForLanding = parameters.getBoolean("wait_for_landing", true);
	}

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
