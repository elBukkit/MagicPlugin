package com.elmakers.mine.bukkit.action.builtin;

import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class TameAction extends BaseSpellAction {
    private boolean own = true;
    private boolean persist = true;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        own = parameters.getBoolean("own", true);
        persist = parameters.getBoolean("persist", true);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Player player = context.getMage().getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }
        Entity entity = context.getTargetEntity();
        boolean tamed = CompatibilityLib.getCompatibilityUtils().tame(entity, player);
        boolean owned = false;
        if (own) {
            EntityData mob = context.getController().getMob(entity);
            if (mob != null && mob.isOwnable()) {
                UUID existingOwner = CompatibilityLib.getCompatibilityUtils().getOwnerId(entity);
                if (existingOwner == null) {
                    CompatibilityLib.getCompatibilityUtils().setOwner(entity, player);
                    owned = true;
                }
            }
        }
        if (tamed || owned) {
            if (persist) {
                CompatibilityLib.getCompatibilityUtils().setPersist(entity, true);
                CompatibilityLib.getCompatibilityUtils().setRemoveWhenFarAway(entity, false);
            }
            return SpellResult.CAST;
        }
        return SpellResult.NO_TARGET;
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
