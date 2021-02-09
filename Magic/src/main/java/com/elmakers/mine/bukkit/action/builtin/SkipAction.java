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
    private int skipDuration;
    private boolean repeatSkip;
    private Long targetTime;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        skipDuration = parameters.getInt("until", 0);
        skipCount = parameters.getInt("skip", skipDuration > 0 ? 0 : 1);
        repeatSkip = parameters.getBoolean("repeat_skip", true);
        skipCounter = 0;
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        targetTime = null;
    }

    @Override
    public SpellResult step(CastContext context) {
        if (targetTime == null && skipDuration > 0) {
            targetTime = System.currentTimeMillis() + skipDuration;
            return SpellResult.PENDING;
        }
        if (skipDuration > 0 && System.currentTimeMillis() < targetTime) {
            return SpellResult.PENDING;
        }
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
