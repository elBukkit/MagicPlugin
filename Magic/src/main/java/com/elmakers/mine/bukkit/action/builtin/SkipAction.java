package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class SkipAction extends CompoundAction
{
    private int skipCount;
    private int skipCounter;
    private boolean repeatSkip;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        skipCount = parameters.getInt("skip", 1);
        repeatSkip = parameters.getBoolean("repeat_skip", true);
        skipCounter = 0;
    }

    @Override
    public SpellResult step(CastContext context) {
        if (skipCounter++ < skipCount)
        {
            return SpellResult.NO_ACTION;
        }
        if (repeatSkip)
        {
            skipCounter = 0;
        }
        return startActions();
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("skip");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        super.getParameterOptions(spell, parameterKey, examples);

        if (parameterKey.equals("skip")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        }
    }
}
