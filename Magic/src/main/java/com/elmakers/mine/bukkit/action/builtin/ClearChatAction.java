package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

/**
 * Copyright (c) 2013-2018 Tyler Grissom
 */
public class ClearChatAction extends BaseSpellAction {

    private String message;
    private int messageCount;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        Messages messages = context.getController().getMessages();
        String messageEntry = parameters.getString("message");

        this.message = ChatColor.translateAlternateColorCodes('&', messages.get(messageEntry, messageEntry));
        this.messageCount = parameters.getInt("message_count", 100);

        if (messageCount <= 0) {
            this.messageCount = 1;
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (!(context.getTargetEntity() instanceof Player)) return SpellResult.PLAYER_REQUIRED;

        Player p = ((Player) context.getTargetEntity());

        for (int i = 0; i < messageCount; i++) {
            p.sendMessage(message);
        }

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }
}
