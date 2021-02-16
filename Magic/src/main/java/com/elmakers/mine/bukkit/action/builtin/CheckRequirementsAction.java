package com.elmakers.mine.bukkit.action.builtin;

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
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        sendMessage = parameters.getBoolean("send_message");
        requirements = ConfigurationUtils.getRequirements(parameters);
        if (requirements == null || requirements.isEmpty()) {
            context.getLogger().warning("CheckRequirements action missing requirements in spell " + context.getName());
        }
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        if (requirements == null) {
            return true;
        }
        String message = context.getController().checkRequirements(context, requirements);
        if (sendMessage && message != null) {
            context.getMage().sendMessage(message);
        }
        return message == null;
    }
}
