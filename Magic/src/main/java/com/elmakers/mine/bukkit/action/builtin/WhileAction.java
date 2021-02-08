package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
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
        requirements = new ArrayList<>();
        Collection<ConfigurationSection> requirementConfigurations = ConfigurationUtils.getNodeList(parameters, "requirements");
        if (requirementConfigurations != null) {
            for (ConfigurationSection requirementConfiguration : requirementConfigurations) {
                requirements.add(new Requirement(requirementConfiguration));
            }
        }
        ConfigurationSection singleConfiguration = ConfigurationUtils.getConfigurationSection(parameters, "requirement");
        if (singleConfiguration != null) {
            requirements.add(new Requirement(singleConfiguration));
        }
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
        String message = context.getController().checkRequirements(context, requirements);
        if (message != null) {
            return SpellResult.NO_ACTION;
        }
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
