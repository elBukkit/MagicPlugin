package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class RepeatAction extends CompoundAction
{
    private boolean infinite;
    private int count;
    private int current;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        String repeatString = parameters.getString("repeat", "");
        if (repeatString.equals("infinite") || repeatString.equals("forever") || repeatString.equals("infinity")) {
            infinite = true;
        } else {
            count = parameters.getInt("repeat", 2);
        }
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        current = 0;
    }

    @Override
    public SpellResult step(CastContext context) {
        // special case for variable repeat counts
        if (count == 0 && !infinite) return SpellResult.NO_ACTION;
        return startActions();
    }

    @Override
    public boolean next(CastContext context) {
        current++;
        return infinite || current < count;
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
            examples.add("infinite");
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        }
    }

    @Override
    public int getActionCount() {
        return count * super.getActionCount();
    }
}
