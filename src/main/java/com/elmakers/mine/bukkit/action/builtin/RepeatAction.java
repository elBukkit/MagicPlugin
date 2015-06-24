package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
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
        count = parameters.getInt("repeat", 2);
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
            SpellResult actionResult = super.perform(context);
            result = result.min(actionResult);
            if (actionResult.isStop())
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
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("repeat");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        super.getParameterOptions(spell, parameterKey, examples);

        if (parameterKey.equals("repeat")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        }
    }

    @Override
    public int getActionCount() {
        return count * actions.getActionCount();
    }
}
