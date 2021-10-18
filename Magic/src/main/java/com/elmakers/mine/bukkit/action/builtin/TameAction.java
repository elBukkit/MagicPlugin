package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class TameAction extends BaseSpellAction {
    private boolean own = true;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        own = parameters.getBoolean("own", true);
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
                String owner = CompatibilityLib.getEntityMetadataUtils().getString(entity, MagicMetaKeys.OWNER_ID);
                if (owner == null) {
                    CompatibilityLib.getEntityMetadataUtils().setString(entity, MagicMetaKeys.OWNER_ID, player.getUniqueId().toString());
                    owned = true;
                }
            }
        }
        return tamed || owned ? SpellResult.CAST : SpellResult.NO_TARGET;
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
