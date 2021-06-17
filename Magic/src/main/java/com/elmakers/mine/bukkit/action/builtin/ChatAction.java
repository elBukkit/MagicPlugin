package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.google.common.collect.ImmutableList;

public class ChatAction extends BaseSpellAction {

    @Nonnull
    private List<String> messages = ImmutableList.of();

    private String translate(CastContext context, String key) {
        String message = context.getMessage(key, key);
        return CompatibilityLib.getCompatibilityUtils().translateColors(message);
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        List<String> keys = parameters.getStringList("randomized_messages");
        if (!keys.isEmpty()) {
            messages = new ArrayList<>();
            for (String key : keys) {
                messages.add(translate(context, key));
            }
        } else {
            String key = parameters.getString("message", "");
            if (!key.isEmpty()) {
                messages = ImmutableList.of(translate(context, key));
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity target = context.getTargetEntity();
        if (!(target instanceof Player)) {
            return SpellResult.PLAYER_REQUIRED;
        }

        String message;
        switch (messages.size()) {
        case 0:
            return SpellResult.FAIL;
        case 1:
            message = messages.get(0);
            break;
        default:
            Random r = context.getRandom();
            message = messages.get(r.nextInt(messages.size()));
            break;
        }

        Player p = ((Player) target);
        p.chat(message);

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
