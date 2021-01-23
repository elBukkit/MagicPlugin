package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class DelayAction extends BaseSpellAction
{
    private boolean infinite;
    private int delay;
    private Long targetTime;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        delay = parameters.getInt("warmup", 1);
        delay = parameters.getInt("delay", delay);

        infinite = parameters.getString("delay", "").equals("infinite");
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        targetTime = null;
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (targetTime == null) {
            targetTime = System.currentTimeMillis() + delay;
            return SpellResult.PENDING;
        }
        if (infinite || System.currentTimeMillis() < targetTime)
        {
            return SpellResult.PENDING;
        }
        return SpellResult.NO_ACTION;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("delay");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("delay")) {
            examples.add("infinite");
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
