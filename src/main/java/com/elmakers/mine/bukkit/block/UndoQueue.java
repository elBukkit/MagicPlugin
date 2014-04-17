package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.plugins.magic.Mage;
import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.utilities.ConfigurationUtils;

public class UndoQueue
{
	private final LinkedList<BlockList> blockQueue = new LinkedList<BlockList>();
	private final Set<BlockList> 		scheduledBlocks = new HashSet<BlockList>();
	private int                         maxSize    = 0;

	public void add(BlockList blocks)
	{
		if (maxSize > 0 && blockQueue.size() > maxSize)
		{
			BlockList expired = blockQueue.removeFirst();
			expired.commit();
		}
		blocks.prepareForUndo();
		blockQueue.add(blocks);
	}
	
	public void scheduleCleanup(Mage mage, BlockList blocks)
	{
		blocks.prepareForUndo();
		scheduledBlocks.add(blocks);

		blocks.scheduleCleanup(mage);
	}
	
	public void undoScheduled(Mage mage)
	{
		if (scheduledBlocks.size() == 0) return;
		for (BlockList list : scheduledBlocks) {
			list.undoScheduled(mage);
		}
		scheduledBlocks.clear();
	}
	
	public boolean isEmpty()
	{
		 return scheduledBlocks.isEmpty() && blockQueue.isEmpty();
	}
	
	public void removeScheduledCleanup(BlockList blockList)
	{
		scheduledBlocks.remove(blockList);
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

	public boolean undo(Mage mage)
	{
		if (blockQueue.size() == 0)
		{
			return false;
		}

		BlockList blocks = blockQueue.removeLast();
		if (blocks.undo(mage)) {
			return true;
		}
		
		blockQueue.add(blocks);
		return false;
	}

	public boolean undo(Mage mage, Block target)
	{
		BlockList lastActionOnTarget = getLast(target);

		if (lastActionOnTarget == null)
		{
			return false;
		}

		if (lastActionOnTarget.undo(mage)) {
			blockQueue.remove(lastActionOnTarget);
			return true;
		}
		
		return false;
	}
	
	public void load(Mage mage, ConfigurationSection node)
	{
		try {
			if (node == null) return;
			Collection<ConfigurationSection> nodeList = ConfigurationUtils.getNodeList(node, "undo");
			if (nodeList != null) {
				for (ConfigurationSection listNode : nodeList) {
					BlockList list = new BlockList();
					list.load(listNode);
					blockQueue.add(list);
				}
			}
			nodeList = ConfigurationUtils.getNodeList(node, "scheduled");
			if (nodeList != null) {
				for (ConfigurationSection listNode : nodeList) {
					BlockList list = new BlockList();
					list.load(listNode);
					scheduleCleanup(mage, list);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			mage.getController().getPlugin().getLogger().warning("Failed to load undo data: " + ex.getMessage());
		}
	}
	
	public void save(Mage mage, ConfigurationSection node)
	{
		MagicController controller = mage.getController();
		int maxSize = controller.getMaxUndoPersistSize();
		try {
			List<Map<String, Object>> nodeList = new ArrayList<Map<String, Object>>();
			for (BlockList list : blockQueue) {
				if (maxSize > 0 && list.size() > maxSize) {
					controller.getLogger().info("Discarding undo batch, size " + list.size() + " for player " + mage.getName());
					continue;
				}
				Map<String, Object> listNode = new HashMap<String, Object>();
				list.save(listNode);
				nodeList.add(listNode);
			}
			node.set("undo", nodeList);
			nodeList = new ArrayList<Map<String, Object>>();
			for (BlockList list : scheduledBlocks) {
				Map<String, Object> listNode = new HashMap<String, Object>();
				list.save(listNode);
				nodeList.add(listNode);
			}
			node.set("scheduled", nodeList);
		} catch (Exception ex) {
			ex.printStackTrace();
			controller.getLogger().warning("Failed to save undo data: " + ex.getMessage());
		}
	}
	
	public int getSize()
	{
		return blockQueue.size();
	}
	
	public boolean commit()
	{
		if (blockQueue.size() == 0) return false;
		for (BlockList list : blockQueue) {
			list.commit();
		}
		blockQueue.clear();
		return true;
	}
}
