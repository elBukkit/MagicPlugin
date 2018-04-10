package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Copyright Tyler Grissom 2018
 */
public class ChatAction extends BaseSpellAction {

    private String message;

    private List<String> randomizedMessages;

    private boolean translateAltColorCodes;

    private char altColorCode;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        this.message = parameters.getString("message");
        this.randomizedMessages = parameters.getStringList("randomized_messages");
        this.translateAltColorCodes = parameters.getBoolean("translate_alt_color_codes", true);

        // Uhh, this is kind of terrible, but there doesn't appear to be a FileConfiguration#getChar or similar

        this.altColorCode = parameters.getString("alt_color_code", "&").charAt(0);
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (!(context.getTargetEntity() instanceof Player)) return null;

        Player p = ((Player) context.getTargetEntity());

        if (randomizedMessages == null || randomizedMessages.isEmpty()) {
            if (message == null) return SpellResult.FAIL;

            if (translateAltColorCodes) message = ChatColor.translateAlternateColorCodes(altColorCode, message);

            p.chat(message);
        } else {
            if (translateAltColorCodes) {
                for (int i = 0; i < randomizedMessages.size(); i++) {
                    randomizedMessages.set(i, ChatColor.translateAlternateColorCodes(altColorCode, randomizedMessages.get(i)));
                }
            }

            p.chat(randomizedMessages.get(ThreadLocalRandom.current().nextInt(randomizedMessages.size())));
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
