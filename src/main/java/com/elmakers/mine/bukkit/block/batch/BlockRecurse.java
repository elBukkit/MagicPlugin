package com.elmakers.mine.bukkit.block.batch;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.BlockData;
import com.elmakers.mine.bukkit.block.UndoList;
import org.bukkit.configuration.ConfigurationSection;

public class BlockRecurse
{
	protected int maxRecursion = 8;

	public void recurse(Block startBlock, BlockAction recurseAction, ConfigurationSection parameters, UndoList undoList)
	{
		recurse(startBlock, recurseAction, parameters, undoList, null, 0);
	}

	protected void recurse(Block block, BlockAction recurseAction, ConfigurationSection parameters, UndoList undoList, BlockFace nextFace, int rDepth)
	{
		if (nextFace != null)
		{
			block = block.getRelative(nextFace);
		}
		if (undoList != null)
		{
			if (undoList.contains(block))
			{
				return;
			}
			undoList.add(block);
		}

		if (recurseAction.perform(parameters, block) != SpellResult.CAST)
		{
			return;
		}

		if (rDepth < maxRecursion)
		{
			for (BlockFace face : BlockData.FACES)
			{
				if (nextFace == null || nextFace != BlockData.getReverseFace(face))
				{
					recurse(block, recurseAction, parameters, undoList, face, rDepth + 1);
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
