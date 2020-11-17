package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.entity.EntityFoxData;

public class TameAction extends BaseSpellAction
{
    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        if (!(entity instanceof Tameable))
        {
            if (entity.getType().name().equals("FOX")) {
                Player tamer = context.getMage().getPlayer();
                boolean result = EntityFoxData.tame(tamer, entity);
                return result ? SpellResult.CAST : SpellResult.NO_TARGET;
            }
            return SpellResult.PLAYER_REQUIRED;
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
