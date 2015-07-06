package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.Collection;

public class AirSupplyAction extends BaseSpellAction
{
    private int air;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        air = parameters.getInt("air", 0);
    }

	@Override
	public SpellResult perform(CastContext context)
	{
		LivingEntity livingEntity = context.getLivingEntity();
		if (livingEntity == null) {
			return SpellResult.NO_TARGET;
		}

		int airLevel = air;
		if (airLevel > livingEntity.getMaximumAir()) {
			airLevel = livingEntity.getMaximumAir();
		}
		context.registerModified(livingEntity);
		livingEntity.setRemainingAir(airLevel);
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

	@Override
	public void getParameterNames(Spell spell, Collection<String> parameters) {
		super.getParameterNames(spell, parameters);
		parameters.add("air");
	}

	@Override
	public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
		if (parameterKey.equals("air")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
		} else {
			super.getParameterOptions(spell, parameterKey, examples);
		}
	}
}
