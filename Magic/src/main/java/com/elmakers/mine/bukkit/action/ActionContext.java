package com.elmakers.mine.bukkit.action;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ActionContext implements Cloneable {
    private final ConfigurationSection parameters;
    private final SpellAction action;

    public ActionContext(SpellAction action, ConfigurationSection actionParameters)
    {
        this.action = action;
        this.parameters = actionParameters;
    }

    public void initialize(Spell spell, ConfigurationSection baseParameters)
    {
        action.initialize(spell, getEffectiveParameters(baseParameters));
    }

    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        action.prepare(context, getEffectiveParameters(parameters));
    }

    public void start(CastContext context, ConfigurationSection parameters)
    {
        action.start(context, getEffectiveParameters(parameters));
    }

    public SpellResult perform(CastContext context)
    {
        boolean hasTarget = context.getTargetLocation() != null;
        boolean hasEntityTarget = context.getTargetEntity() != null;
        if (action.requiresTarget() && !hasTarget) return SpellResult.NO_TARGET;
        if (action.requiresTargetEntity() && !hasEntityTarget) return SpellResult.NO_TARGET;

        SpellResult result = action.perform(context);
        return action.ignoreResult() && !result.isStop() ? SpellResult.NO_ACTION : result;
    }

    public ConfigurationSection getActionParameters()
    {
        return parameters;
    }

    public ConfigurationSection getEffectiveParameters(ConfigurationSection baseParameters)
    {
        ConfigurationSection effectiveParameters = baseParameters;
        if (this.parameters != null || baseParameters == null) {
            effectiveParameters = ConfigurationUtils.cloneConfiguration(baseParameters);
            String parametersKey = this.parameters == null ? null : this.parameters.getString("parameters");
            ConfigurationSection overrideParameters = parametersKey == null || baseParameters == null ? null : ConfigurationUtils.getConfigurationSection(baseParameters, parametersKey);
            if (overrideParameters != null) {
                ConfigurationUtils.addConfigurations(effectiveParameters, overrideParameters);
            }
            ConfigurationUtils.addConfigurations(effectiveParameters, this.parameters);
        }

        return effectiveParameters;
    }

    public SpellAction getAction() {
        return this.action;
    }

    public void finish(CastContext context) {
        action.finish(context);
    }

    @Override
    public ActionContext clone()
    {
        return new ActionContext((SpellAction)action.clone(), parameters);
    }
}
