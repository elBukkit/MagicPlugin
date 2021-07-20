package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.entity.EntityData;

public class RespawnAction extends CompoundAction {

    @Override
    public SpellResult perform(CastContext context) {
        Entity entity = context.getTargetEntity();
        if (!entity.isDead()) {
            return SpellResult.NO_TARGET;
        }
        MageController controller = context.getController();
        MagicNPC npc = controller.getNPC(entity);
        Entity respawned = null;
        if (npc != null) {
            respawned = npc.respawn();
        } else {
            EntityData entityData = new EntityData(controller, entity);
            respawned = entityData.respawn();
        }
        return respawned == null ? SpellResult.NO_TARGET : SpellResult.CAST;
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
