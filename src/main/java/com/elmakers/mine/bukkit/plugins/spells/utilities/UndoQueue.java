package com.elmakers.mine.bukkit.plugins.spells.utilities;

import java.util.LinkedList;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.persistence.dao.BlockList;


public class UndoQueue
{
	private final LinkedList<BlockList> blockQueue = new LinkedList<BlockList>();
	private int maxSize = 0;
	
	public void add(BlockList blocks)
	{
		if (maxSize > 0 && blockQueue.size() > maxSize)
		{
			blockQueue.removeFirst();
		}
		blockQueue.add(blocks);
	}
	
	public boolean undo()
	{
		if (blockQueue.size() == 0) return false;
		
		BlockList blocks = blockQueue.removeLast();
		if (!blocks.undo())
		{
			blockQueue.add(blocks);
			return false;
		}
		return true;
	}
	
	public boolean undo(Block target)
	{
		BlockList lastActionOnTarget = getLast(target);

		if (lastActionOnTarget == null)
		{
			return false;
		}
		
		blockQueue.remove(lastActionOnTarget);
		lastActionOnTarget.undo();
		
		return true;
	}
	
	public void setMaxSize(int size)
	{
		maxSize = size;
	}
	
	public BlockList getLast()
	{
		if (blockQueue.isEmpty()) return null;
		return blockQueue.getLast();
	}
	
	public BlockList getLast(Block target)
	{

		if (blockQueue.size() == 0) return null;
		for (BlockList blocks : blockQueue)
		{
			if (blocks.contains(target))
			{
				return blocks;
			}
		}
		return null;
	}
}
