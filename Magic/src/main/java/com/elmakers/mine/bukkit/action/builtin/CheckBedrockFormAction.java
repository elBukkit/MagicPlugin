package com.elmakers.mine.bukkit.action.builtin;

import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CheckBedrockFormAction extends CheckTriggerAction {
    private String messageKey;
    private String button1MessageKey;
    private String button2MessageKey;
    private String titleKey;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        // This overwrites whatever was loaded from parameters in CheckTrigger.prepare
        trigger = UUID.randomUUID().toString();
        messageKey = parameters.getString("message_key", "message");
        button1MessageKey = parameters.getString("button1_key", "button1");
        button2MessageKey = parameters.getString("button2_key", "button2");
        titleKey = parameters.getString("title_key", "title");
    }

    @Override
    public SpellResult start(CastContext context) {
        Entity targetEntity = context.getTargetEntity();
        if (!(targetEntity instanceof Player)) {
            return SpellResult.PLAYER_REQUIRED;
        }

        Mage targetMage = context.getController().getMage(targetEntity);
        String message = context.getMessage(messageKey, "");
        String button1Message = context.getMessage(button1MessageKey, "Ok");
        String button2Message = context.getMessage(button2MessageKey, "Cancel");
        String titleMessage = context.getMessage(titleKey, "");
        if (button1Message.isEmpty()) {
            return SpellResult.FAIL;
        }
        String[] buttons = button2Message.isEmpty() ? new String[] {button1Message} : new String[] {button1Message, button2Message};
        // Note that, due to the way Check actions are structured, we can't really make use of the button2 response
        // It will work the same as them closing the dialog without hitting any buttons.
        String[] triggers = new String[] {trigger};
        titleMessage = context.parameterize(titleMessage);
        message = context.parameterize(message);
        targetMage.showModalForm(context.getMage(), titleMessage, message, buttons, triggers);
        return super.start(context);
    }
}
