package com.elmakers.mine.bukkit.utilities;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.block.Block;

import com.elmakers.mine.bukkit.dao.BlockData;
import com.elmakers.mine.bukkit.dao.BlockList;
import com.elmakers.mine.bukkit.plugins.magic.Spells;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

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
	
	public void load(ConfigurationNode node)
	{
		if (node == null) return;
		List<String> keys = node.getKeys();
		for (String key : keys) {
			BlockList list = new BlockList();
			ConfigurationNode listNode = node.getNode(key);
			List<String> listKeys = listNode.getKeys();
			for (String listKey : listKeys) {
				list.add(listNode.getBlockData(listKey));
			}
			blockQueue.add(list);
		}
	}
	
	public void save(ConfigurationNode node)
	{
		int index = 0;
		for (BlockList list : blockQueue) {
			ConfigurationNode listNode = node.createChild(((Integer)index).toString());
			index++;
			int blockIndex = 0;
			for (BlockData data : list.getBlockList()) {
				listNode.setProperty(((Integer)blockIndex).toString(), data);
				blockIndex++;
			}
		}
	}
}
