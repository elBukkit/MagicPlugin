package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

public class CoverAction extends CompoundAction
{
    protected boolean targetAbove = false;
    protected boolean targetHighest = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        targetAbove = parameters.getBoolean("target_above", false);
        targetHighest = parameters.getBoolean("target_highest", false);
    }

    @Override
    public SpellResult step(CastContext context) {
        Block targetBlock = context.getTargetBlock();
        if (targetHighest) {
            targetBlock = targetBlock.getWorld().getHighestBlockAt(targetBlock.getLocation());
        } else {
            targetBlock = context.findSpaceAbove(targetBlock);
            targetBlock = context.findBlockUnder(targetBlock);
        }
        Block coveringBlock = targetBlock.getRelative(BlockFace.UP);
        if (context.isTransparent(targetBlock) || !context.isTransparent(coveringBlock)) {
            skippedActions(context);
            return SpellResult.NO_TARGET;
        }
        if (targetAbove) {
            actionContext.setTargetLocation(coveringBlock.getLocation());
        } else {
            actionContext.setTargetLocation(targetBlock.getLocation());
        }
        return startActions();
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
