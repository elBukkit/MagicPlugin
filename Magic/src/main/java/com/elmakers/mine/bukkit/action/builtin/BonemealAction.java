package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class BonemealAction extends BaseSpellAction {
    @Override
    public SpellResult perform(CastContext context) {
        Location target = context.getTargetLocation();
        Block targetBlock = target.getBlock();
        if (!context.isDestructible(targetBlock)) {
            return SpellResult.NO_TARGET;
        }
        if (!context.hasBuildPermission(targetBlock)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }
        context.registerForUndo(targetBlock);
        if (!CompatibilityUtils.applyBonemeal(target)) {
            return SpellResult.NO_TARGET;
        }

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
