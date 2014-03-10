package com.elmakers.mine.bukkit.blocks;

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

import com.elmakers.mine.bukkit.plugins.magic.Mage;
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
	
	public void scheduleCleanup(Mage mage, BlockList blocks)
	{
		scheduledBlocks.add(blocks);
		
		Plugin plugin = mage.getController().getPlugin();
		Server server = plugin.getServer();
		BukkitScheduler scheduler = server.getScheduler();

		// scheduler works in ticks- 20 ticks per second.
		long ticksToLive = blocks.getTimeToLive() * 20 / 1000;
		scheduler.scheduleSyncDelayedTask(plugin, new CleanupBlocksTask(mage, blocks), ticksToLive);
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
		blocks.undo(mage);
		return true;
	}

	public boolean undo(Mage mage, Block target)
	{
		BlockList lastActionOnTarget = getLast(target);

		if (lastActionOnTarget == null)
		{
			return false;
		}

		blockQueue.remove(lastActionOnTarget);
		lastActionOnTarget.undo(mage);

		return true;
	}
	
	public void load(Mage mage, ConfigurationNode node)
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
				scheduleCleanup(mage, list);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			mage.getController().getPlugin().getLogger().warning("Failed to load undo data: " + ex.getMessage());
		}
	}
	
	public void save(Mage mage, ConfigurationNode node)
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
		
		blockQueue.clear();
		return true;
	}
}
