package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;

public class ChatAction extends BaseSpellAction {

    @Nonnull
    private String message =  "";

    private String translate(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);

        this.message = context.getMessage(parameters.getString("message"), translate(parameters.getString("message")));

        List<String> l = parameters.getStringList("randomized_messages");

        if (!l.isEmpty()) this.message = translate(l.get(context.getRandom().nextInt(l.size())));
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (!(context.getTargetEntity() instanceof Player)) return SpellResult.PLAYER_REQUIRED;

        Player p = ((Player) context.getTargetEntity());

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
