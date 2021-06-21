package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class CycleEntityAction extends BaseSpellAction {

    @Override
    public SpellResult perform(CastContext context) {
        Entity entity = context.getTargetEntity();
        EntityExtraData extraData = CompatibilityLib.getEntityUtils().getExtraData(context.getController(), entity);
        if (extraData == null || !extraData.canCycle(entity)) {
            return SpellResult.NO_TARGET;
        }
        context.registerModified(entity);
        boolean success = extraData.cycle(entity);
        return success ? SpellResult.CAST : SpellResult.NO_TARGET;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}
