package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class CheckRequirementsAction extends CheckAction {
    private Collection<Requirement> requirements;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        requirements = new ArrayList<>();
        Collection<ConfigurationSection> requirementConfigurations = ConfigurationUtils.getNodeList(parameters, "requirements");
        if (requirementConfigurations != null) {
            for (ConfigurationSection requirementConfiguration : requirementConfigurations) {
                requirements.add(new Requirement(requirementConfiguration));
            }
        }
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        return context.getController().checkRequirements(context, requirements) == null;
    }
}