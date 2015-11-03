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
	public SpellResult step(CastContext context) {
        return startActions();
	}

    @Override
    public boolean next(CastContext context) {
        current++;
        return current < count;
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
        return count * super.getActionCount();
    }
}
