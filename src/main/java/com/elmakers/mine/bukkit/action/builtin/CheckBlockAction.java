package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.block.Block;

public class CheckBlockAction extends BaseSpellAction {
    @SuppressWarnings("deprecation")
    @Override
    public SpellResult perform(CastContext context) {
        MaterialBrush brush = context.getBrush();
        Block block = context.getTargetBlock();
        if (brush != null && brush.isErase()) {
            if (!context.hasBreakPermission(block)) {
                return SpellResult.STOP;
            }
        } else {
            if (!context.hasBuildPermission(block)) {
                return SpellResult.STOP;
            }
        }
        if (!context.isDestructible(block)) {
            return SpellResult.STOP;
        }
        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}