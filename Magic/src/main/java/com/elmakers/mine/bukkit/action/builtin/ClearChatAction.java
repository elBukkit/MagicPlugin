package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class ClearChatAction extends BaseSpellAction {

    @Override
    public SpellResult perform(CastContext context) {
        if (!(context.getTargetEntity() instanceof Player)) return SpellResult.PLAYER_REQUIRED;

        Player p = (Player) context.getTargetEntity();

        for (int i = 0; i < 100; i++) {
            p.sendMessage(" ");
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
