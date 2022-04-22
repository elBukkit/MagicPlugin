package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class CheckRequirementsAction extends CheckAction {
    private Collection<Requirement> requirements;
    private boolean sendMessage;
    private boolean useTarget;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        useTarget = parameters.getBoolean("use_target");
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
        if (useTarget) {
            Entity targetEntity = context.getTargetEntity();
            if (targetEntity == null) {
                return false;
            }
            Entity sourceEntity = context.getEntity();
            Location sourceLocation = context.getLocation();
            Mage targetMage = context.getController().getMage(targetEntity);
            context = new com.elmakers.mine.bukkit.action.CastContext(context, targetMage, targetEntity, context.getTargetLocation());
            context.setTargetEntity(sourceEntity);
            context.setTargetLocation(sourceLocation);
        }
        String message = context.getController().checkRequirements(context, requirements);
        if (sendMessage && message != null) {
            context.getMage().sendMessage(message);
        }
        return message == null;
    }
}
