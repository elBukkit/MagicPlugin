package com.elmakers.mine.bukkit.action.builtin;

import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CheckChatClickAction extends CheckTriggerAction {
    private String messageKey;
    private String clickMessageKey;
    private String hoverMessageKey;
    private String clickTemplateKey;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        // This overwrites whatever was loaded from parameters in CheckTrigger.prepare
        trigger = UUID.randomUUID().toString();
        messageKey = parameters.getString("message_key", "message");
        clickMessageKey = parameters.getString("click_message_key", "click_message");
        hoverMessageKey = parameters.getString("Hover_message_key", "hover_message");
        clickTemplateKey = parameters.getString("click_template_key", "click_template");
    }

    @Override
    public SpellResult start(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (!(targetEntity instanceof Player)) {
            return SpellResult.PLAYER_REQUIRED;
        }

        Mage targetMage = context.getController().getMage(targetEntity);
        String sourceId = context.getMage().getId();
        String message = context.getMessage(messageKey, "$click");
        String clickMessage = context.getMessage(clickMessageKey, "(Click Here)");
        String hoverMessage = context.getMessage(hoverMessageKey, "");
        String clickTemplate = context.getMessage(clickTemplateKey, "`{\"text\":\"$message\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"$hover\"}, \"clickEvent\":{\"action\":\"run_command\",\"value\":\"/$command\"}}`");
        clickTemplate = clickTemplate.replace("$message", clickMessage);
        clickTemplate = clickTemplate.replace("$hover", hoverMessage);
        clickTemplate = clickTemplate.replace("$command", "mtrigger " + sourceId + " " + trigger);
        message = message.replace("$click", clickTemplate);
        message = context.parameterize(message);
        targetMage.sendMessage(message);
        return super.start(context);
    }
}
