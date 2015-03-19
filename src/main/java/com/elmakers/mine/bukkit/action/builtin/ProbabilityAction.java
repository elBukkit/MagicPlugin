package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public class ProbabilityAction extends CompoundAction
{
    private double probability;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        probability = parameters.getDouble("probability", 0.5);
    }

	@Override
    public SpellResult perform(CastContext context)
	{
		SpellResult result = SpellResult.NO_ACTION;
        if (context.getRandom().nextDouble() <= probability) {
            result = result.min(performActions(actionContext));
        }

		return result;
	}

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("probability");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("probability")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_PERCENTAGES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
