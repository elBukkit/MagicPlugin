package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public class DelayAction extends CompoundAction
{
    private int delay;
    private long targetTime;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        delay = parameters.getInt("delay", 1);
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        targetTime = System.currentTimeMillis() + delay;
    }

	@Override
	public SpellResult perform(CastContext context) {
        if (System.currentTimeMillis() < targetTime)
        {
            return SpellResult.PENDING;
        }
		return performActions(context);
	}

    @Override
    public void getParameterNames(Collection<String> parameters)
    {
        super.getParameterNames(parameters);
        parameters.add("delay");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("delay")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        }
    }
}
