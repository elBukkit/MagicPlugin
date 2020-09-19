package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class TameAction extends BaseSpellAction
{
    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        if (!(entity instanceof Tameable))
        {
            if (CompatibilityUtils.isFox(entity)) {
                Player tamer = context.getMage().getPlayer();
                if (tamer != null) {
                    if (CompatibilityUtils.isFirstTrustedPlayer(entity, tamer)) {
                        return SpellResult.NO_TARGET;
                    }
                    if (CompatibilityUtils.setFirstTrustedPlayer(entity, tamer)) {
                        return SpellResult.CAST;
                    }
                    return SpellResult.FAIL;
                }
            }
            return SpellResult.NO_TARGET;
        }

        Tameable tameable = (Tameable)entity;
        if (tameable.isTamed()) {
            return SpellResult.NO_TARGET;
        }
        tameable.setTamed(true);
        Player tamer = context.getMage().getPlayer();
        if (tamer != null) {
            tameable.setOwner(tamer);
        }
        return SpellResult.CAST;
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
