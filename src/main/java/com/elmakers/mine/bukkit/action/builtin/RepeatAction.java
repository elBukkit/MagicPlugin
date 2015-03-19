package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;

public class RepeatAction extends CompoundAction
{
    private int count;
    private int current;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        count = parameters.getInt("count", 2);
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        current = 0;
    }

	@Override
	public SpellResult perform(CastContext context) {
        SpellResult result = SpellResult.NO_ACTION;
        while (current < count)
        {
            SpellResult actionResult = performActions(actionContext);
            result = result.min(actionResult);
            if (actionResult == SpellResult.PENDING)
            {
                break;
            }
            current++;
            if (current < count)
            {
                super.reset(context);
            }
        }

		return result;
	}
    @Override
    public void getParameterNames(Collection<String> parameters)
    {
        super.getParameterNames(parameters);
        parameters.add("count");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("count")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        }
    }

    @Override
    public int getActionCount() {
        return count * actions.getActionCount();
    }
}
