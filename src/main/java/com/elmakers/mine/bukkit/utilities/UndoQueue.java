package com.elmakers.mine.bukkit.utilities;

import java.util.LinkedList;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spells;

public class UndoQueue
{
	private final LinkedList<BlockList> blockQueue = new LinkedList<BlockList>();
	private int                         maxSize    = 0;

	public void add(BlockList blocks)
	{
		if (maxSize > 0 && blockQueue.size() > maxSize)
		{
			blockQueue.removeFirst();
		}
		blockQueue.add(blocks);
	}

	public BlockList getLast()
	{
		if (blockQueue.isEmpty())
		{
			return null;
		}
		return blockQueue.getLast();
	}

	public BlockList getLast(Block target)
	{

		if (blockQueue.size() == 0)
		{
			return null;
		}
		for (BlockList blocks : blockQueue)
		{
			if (blocks.contains(target))
			{
				return blocks;
			}
		}
		return null;
	}

	public void setMaxSize(int size)
	{
		maxSize = size;
	}

	public boolean undo(Spells spells)
	{
		if (blockQueue.size() == 0)
		{
			return false;
		}

		BlockList blocks = blockQueue.removeLast();
		blocks.undo(spells);
		return true;
	}

	public boolean undo(Spells spells, Block target)
	{
		BlockList lastActionOnTarget = getLast(target);

		if (lastActionOnTarget == null)
		{
			return false;
		}

		blockQueue.remove(lastActionOnTarget);
		lastActionOnTarget.undo(spells);

		return true;
	}
}
