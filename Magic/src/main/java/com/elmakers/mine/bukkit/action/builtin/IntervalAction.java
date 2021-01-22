package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class IntervalAction extends CompoundAction
{
    private boolean infinite;
    private int duration;
    private int interval;
    private long startTime;
    private long stepTime;

    public IntervalAction() {
        super();
        pauseOnNext = true;
    }

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters) {
        super.processParameters(context, parameters);
        if (parameters.getString("duration", "").equals("infinite")) {
            infinite = true;
        } else {
            duration = parameters.getInt("duration", 0);
        }
        interval = parameters.getInt("interval", 0);
    }

    @Override
    public void reset(CastContext context)
    {
        super.reset(context);
        startTime = System.currentTimeMillis();
        stepTime = 0;
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (stepTime > 0 && System.currentTimeMillis() < stepTime + interval) {
            return SpellResult.PENDING;
        }
        stepTime = System.currentTimeMillis();
        return super.perform(context);
    }

    @Override
    public SpellResult step(CastContext context) {
        return startActions();
    }

    @Override
    public boolean next(CastContext context) {
        return infinite || System.currentTimeMillis() < startTime + duration;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("duration");
        parameters.add("interval");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        super.getParameterOptions(spell, parameterKey, examples);

        if (parameterKey.equals("duration")) {
            examples.add("infinite");
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        }
        if (parameterKey.equals("interval")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        }
    }

    @Override
    public int getActionCount() {
        int interval = Math.max(this.interval, 50);
        return (1 + duration / interval) * super.getActionCount();
    }
}
