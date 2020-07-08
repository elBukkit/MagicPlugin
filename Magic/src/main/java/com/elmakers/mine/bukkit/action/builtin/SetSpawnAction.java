package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class SetSpawnAction extends BaseSpellAction {
    @Override
    public SpellResult perform(CastContext context) {
        Player player = context.getMage().getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        player.setBedSpawnLocation(context.getTargetLocation(), true);

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
