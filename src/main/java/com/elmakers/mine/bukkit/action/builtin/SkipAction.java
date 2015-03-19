package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public class SkipAction extends CompoundAction
{
    private int skipCount;
    private int skipCounter;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        skipCount = parameters.getInt("skip", 1);
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        skipCounter = 0;
    }

	@Override
	public SpellResult perform(CastContext context) {
        if (skipCounter++ <= skipCount)
        {
            return SpellResult.PENDING;
        }
		return performActions(context);
	}

    @Override
    public void getParameterNames(Collection<String> parameters)
    {
        super.getParameterNames(parameters);
        parameters.add("skip");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("skip")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        }
    }
}
