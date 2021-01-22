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
    private boolean sendMessage;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        super.processParameters(context, parameters);
        sendMessage = parameters.getBoolean("send_message");
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
    protected boolean isAllowed(CastContext context) {
        String message = context.getController().checkRequirements(context, requirements);
        if (sendMessage && message != null) {
            context.getMage().sendMessage(message);
        }
        return message == null;
    }
}
