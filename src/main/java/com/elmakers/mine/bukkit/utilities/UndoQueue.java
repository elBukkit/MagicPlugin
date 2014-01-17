package com.elmakers.mine.bukkit.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.elmakers.mine.bukkit.blocks.BlockList;
import com.elmakers.mine.bukkit.blocks.CleanupBlocksTask;
import com.elmakers.mine.bukkit.plugins.magic.MagicController;
import com.elmakers.mine.bukkit.utilities.borrowed.ConfigurationNode;

public class UndoQueue
{
	private final LinkedList<BlockList> blockQueue = new LinkedList<BlockList>();
	private final Set<BlockList> 		scheduledBlocks = new HashSet<BlockList>();
	private int                         maxSize    = 0;

	public void add(BlockList blocks)
	{
		if (maxSize > 0 && blockQueue.size() > maxSize)
		{
			blockQueue.removeFirst();
		}
		blockQueue.add(blocks);
	}
	
	public void scheduleCleanup(MagicController spells, BlockList blocks)
	{
		scheduledBlocks.add(blocks);
		
		Plugin plugin = spells.getPlugin();
		Server server = plugin.getServer();
		BukkitScheduler scheduler = server.getScheduler();

		// scheduler works in ticks- 20 ticks per second.
		long ticksToLive = blocks.getTimeToLive() * 20 / 1000;
		scheduler.scheduleSyncDelayedTask(plugin, new CleanupBlocksTask(this, spells, blocks), ticksToLive);
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

	public boolean undo(MagicController spells)
	{
		if (blockQueue.size() == 0)
		{
			return false;
		}

		BlockList blocks = blockQueue.removeLast();
		blocks.undo(spells);
		return true;
	}

	public boolean undo(MagicController spells, Block target)
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
	
	public void load(MagicController spells, ConfigurationNode node)
	{
		try {
			if (node == null) return;
			List<ConfigurationNode> nodeList = node.getNodeList("undo", null);
			for (ConfigurationNode listNode : nodeList) {
				BlockList list = new BlockList();
				list.load(listNode);
				blockQueue.add(list);
			}
			nodeList = node.getNodeList("scheduled", null);
			for (ConfigurationNode listNode : nodeList) {
				BlockList list = new BlockList();
				list.load(listNode);
				scheduleCleanup(spells, list);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			spells.getPlugin().getLogger().warning("Failed to load undo data: " + ex.getMessage());
		}
	}
	
	public void save(MagicController spells, ConfigurationNode node)
	{
		try {
			List<Map<String, Object>> nodeList = new ArrayList<Map<String, Object>>();
			for (BlockList list : blockQueue) {
				Map<String, Object> listNode = new HashMap<String, Object>();
				list.save(listNode);
				nodeList.add(listNode);
			}
			node.setProperty("undo", nodeList);
			nodeList = new ArrayList<Map<String, Object>>();
			for (BlockList list : scheduledBlocks) {
				Map<String, Object> listNode = new HashMap<String, Object>();
				list.save(listNode);
				nodeList.add(listNode);
			}
			node.setProperty("scheduled", nodeList);
		} catch (Exception ex) {
			ex.printStackTrace();
			spells.getPlugin().getLogger().warning("Failed to save undo data: " + ex.getMessage());
		}
	}
	
	public int getSize()
	{
		return blockQueue.size();
	}
	
	public void commit()
	{
		blockQueue.clear();
	}
}
