package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;

public class CheckTriggerAction extends CheckAction {
    private boolean sinceStartOfCast;
    private long startTime;
    private String trigger;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        trigger = parameters.getString("trigger", "");
        sinceStartOfCast = parameters.getBoolean("since_start_of_cast", false);
    }

    @Override
    public void reset(CastContext context) {
        startTime = System.currentTimeMillis();
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        Long lastTrigger = context.getMage().getLastTrigger(trigger);
        long startTime = sinceStartOfCast ? context.getStartTime() : this.startTime;
        return (lastTrigger != null && lastTrigger > startTime);
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("trigger");
    }
}
