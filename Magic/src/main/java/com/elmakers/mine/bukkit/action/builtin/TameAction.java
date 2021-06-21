package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class TameAction extends BaseSpellAction
{
    @Override
    public SpellResult perform(CastContext context)
    {
        Player player = context.getMage().getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        Entity entity = context.getTargetEntity();
        boolean tamed = CompatibilityLib.getCompatibilityUtils().tame(entity, player);
        return tamed ? SpellResult.CAST : SpellResult.NO_TARGET;
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
