package com.elmakers.mine.bukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class UndoQueue implements com.elmakers.mine.bukkit.api.block.UndoQueue
{
	private final Plugin				plugin;
	private final LinkedList<UndoList> 	changeQueue = new LinkedList<UndoList>();
	private final Set<UndoList> 		scheduledBlocks = new HashSet<UndoList>();
	private int                         maxSize    = 0;

	public UndoQueue(Plugin plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public void add(UndoList blocks)
	{
		if (maxSize > 0 && changeQueue.size() > maxSize)
		{
			UndoList expired = changeQueue.removeFirst();
			expired.commit();
		}
		changeQueue.add(blocks);
	}
	
	public void scheduleCleanup(Mage mage, UndoList blocks)
	{
		scheduledBlocks.add(blocks);
		blocks.scheduleCleanup(mage);
	}
	
	public void undoScheduled(Mage mage)
	{
		if (scheduledBlocks.size() == 0) return;
		for (UndoList list : scheduledBlocks) {
			list.undoScheduled(mage);
		}
		scheduledBlocks.clear();
	}
	
	public boolean isEmpty()
	{
		 return scheduledBlocks.isEmpty() && changeQueue.isEmpty();
	}
	
	public void removeScheduledCleanup(UndoList blockList)
	{
		scheduledBlocks.remove(blockList);
	}

	public UndoList getLast()
	{
		if (changeQueue.isEmpty())
		{
			return null;
		}
		return changeQueue.getLast();
	}

	public UndoList getLast(Block target)
	{
		if (changeQueue.size() == 0)
		{
			return null;
		}
		for (UndoList blocks : changeQueue)
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
		if (changeQueue.size() == 0)
		{
			return false;
		}

		UndoList blocks = changeQueue.removeLast();
		if (blocks.undo(mage)) {
			return true;
		}
		
		changeQueue.add(blocks);
		return false;
	}

	public boolean undo(Mage mage, Block target)
	{
		UndoList lastActionOnTarget = getLast(target);

		if (lastActionOnTarget == null)
		{
			return false;
		}

		if (lastActionOnTarget.undo(mage)) {
			changeQueue.remove(lastActionOnTarget);
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
					UndoList list = new com.elmakers.mine.bukkit.block.UndoList(plugin);
					list.load(listNode);
					changeQueue.add(list);
				}
			}
			nodeList = ConfigurationUtils.getNodeList(node, "scheduled");
			if (nodeList != null) {
				for (ConfigurationSection listNode : nodeList) {
					UndoList list = new com.elmakers.mine.bukkit.block.UndoList(plugin);
					list.load(listNode);
					scheduleCleanup(mage, list);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			mage.getController().getLogger().warning("Failed to load undo data: " + ex.getMessage());
		}
	}
	
	public void save(Mage mage, ConfigurationSection node)
	{
		MageController controller = mage.getController();
		int maxSize = controller.getMaxUndoPersistSize();
		try {
			int discarded = 0;
			List<Map<String, Object>> nodeList = new ArrayList<Map<String, Object>>();
			for (UndoList list : changeQueue) {
				if (maxSize > 0 && list.size() > maxSize) {
					discarded++;
					continue;
				}
				MemoryConfiguration listNode = new MemoryConfiguration();		
				list.save(listNode);
				nodeList.add(listNode.getValues(true));
			}
			if (discarded > 0) {
				controller.getLogger().info("Not saving " + discarded + " undo batches for player " + mage.getName() + ", over max size of " + maxSize);
			}
			node.set("undo", nodeList);
			nodeList = new ArrayList<Map<String, Object>>();
			for (UndoList list : scheduledBlocks) {
				MemoryConfiguration listNode = new MemoryConfiguration();				
				list.save(listNode);
				nodeList.add(listNode.getValues(true));
			}
			node.set("scheduled", nodeList);
		} catch (Exception ex) {
			ex.printStackTrace();
			controller.getLogger().warning("Failed to save undo data: " + ex.getMessage());
		}
	}
	
	public int getSize()
	{
		return changeQueue.size();
	}
	
	public boolean commit()
	{
		if (changeQueue.size() == 0) return false;
		for (UndoList list : changeQueue) {
			list.commit();
		}
		changeQueue.clear();
		return true;
	}
}
