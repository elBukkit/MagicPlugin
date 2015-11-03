package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class CoverAction extends CompoundAction
{
	@Override
	public SpellResult step(CastContext context) {
        Block targetBlock = context.getTargetBlock();
        targetBlock = context.findSpaceAbove(targetBlock);
        targetBlock = context.findBlockUnder(targetBlock);
        Block coveringBlock = targetBlock.getRelative(BlockFace.UP);
        if (context.isTransparent(targetBlock.getType()) || !context.isTransparent(coveringBlock.getType())) {
            skippedActions(context);
            return SpellResult.NO_TARGET;
        }
        actionContext.setTargetLocation(targetBlock.getLocation());
        return startActions();
	}

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
