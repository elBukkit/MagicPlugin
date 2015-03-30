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
    private boolean pending = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        skipCount = parameters.getInt("skip", 1);
        skipCounter = 0;
        pending = false;
    }

	@Override
	public SpellResult perform(CastContext context) {
        if (!pending && skipCounter++ <= skipCount)
        {
            return SpellResult.NO_ACTION;
        }
        skipCounter = 0;
        if (!pending)
        {
            super.reset(context);
        }
        SpellResult result = super.perform(context);
        pending = result == SpellResult.PENDING;
		return result;
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
