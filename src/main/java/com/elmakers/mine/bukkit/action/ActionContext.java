package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public class ActionContext implements Cloneable {
    private final ConfigurationSection parameters;
    private final SpellAction action;

    public ActionContext(SpellAction action, ConfigurationSection actionParameters)
    {
        this.action = action;
        this.parameters = actionParameters;
    }

    public void initialize(ConfigurationSection baseParameters)
    {
        action.initialize(getEffectiveParameters(baseParameters));
    }

    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        action.prepare(context, getEffectiveParameters(parameters));
    }

    public SpellResult perform(CastContext context)
    {
        return action.perform(context);
    }

    public ConfigurationSection getEffectiveParameters(ConfigurationSection baseParameters)
    {
        ConfigurationSection effectiveParameters = baseParameters;
        if (this.parameters != null || baseParameters == null || baseParameters.contains("actions")) {
            effectiveParameters = new MemoryConfiguration();
            ConfigurationUtils.addConfigurations(effectiveParameters, baseParameters);
            effectiveParameters.set("actions", null);
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
    public Object clone()
    {
        return new ActionContext((SpellAction)action.clone(), parameters);
    }
}
