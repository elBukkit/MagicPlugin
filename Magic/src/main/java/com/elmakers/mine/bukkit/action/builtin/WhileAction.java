package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class WhileAction extends CompoundAction {
    private Collection<Requirement> requirements;
    private int interval;
    private long startTime;
    private long stepTime;

    public WhileAction() {
        super();
        pauseOnNext = true;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        interval = parameters.getInt("interval", 0);
        requirements = ConfigurationUtils.getRequirements(parameters);
        if (requirements == null || requirements.isEmpty()) {
            context.getLogger().warning("While action missing requirements in spell " + context.getName());
        }
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        startTime = System.currentTimeMillis();
        stepTime = 0;
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (requirements == null) {
            return SpellResult.FAIL;
        }
        String message = context.getController().checkRequirements(context, requirements);
        if (message != null) {
            return SpellResult.NO_ACTION;
        }
        if (stepTime > 0 && System.currentTimeMillis() < startTime + interval) {
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
        if (requirements == null) {
            return false;
        }
        String message = context.getController().checkRequirements(context, requirements);
        return message == null;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("requirements");
        parameters.add("interval");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        super.getParameterOptions(spell, parameterKey, examples);
        if (parameterKey.equals("interval")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_DURATIONS));
        }
    }
}
