package com.elmakers.mine.bukkit.block.batch;

import com.elmakers.mine.bukkit.action.ActionContext;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockData;

public class BlockRecurse
{
	protected int maxRecursion = 8;

	public void recurse(ActionContext action, CastContext context)
	{
		recurse(context.getTargetBlock(), action, context, null, 0);
	}

	protected void recurse(Block block, ActionContext recurseAction, CastContext context, BlockFace nextFace, int rDepth)
	{
		if (nextFace != null)
		{
			block = block.getRelative(nextFace);
		}
        UndoList undoList = context.getUndoList();
		if (undoList != null)
		{
			if (undoList.contains(block))
			{
				return;
			}
			undoList.add(block);
		}

        context.setTargetLocation(block.getLocation());
		if (recurseAction.perform(context) != SpellResult.CAST)
		{
			return;
		}

		if (rDepth < maxRecursion)
		{
			for (BlockFace face : BlockData.FACES)
			{
				if (nextFace == null || nextFace != BlockData.getReverseFace(face))
				{
					recurse(block, recurseAction, context, face, rDepth + 1);
				}
			}
		}
	}

	public int getMaxRecursion() {
		return maxRecursion;
	}

	public void setMaxRecursion(int maxRecursion) {
		this.maxRecursion = maxRecursion;
	}
}
