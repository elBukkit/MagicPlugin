package com.elmakers.mine.bukkit.block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.plugins.magic.Mage;

/**
 * 
 * Implements a Collection of Blocks, for quick getting/putting while iterating
 * over a set or area of blocks.
 * 
 * This stores BlockData objects, which are hashable via their Persisted
 * inheritence, and their LocationData id (which itself has a hash function
 * based on world name and BlockVector's hash function)
 * 
 * @author NathanWolf
 * 
 */
public class BlockList implements Collection<BlockData>, Serializable
{
	/**
	 * Default serial id, in case you want to serialize this (probably shouldn't
	 * though!)
	 * 
	 * Persist it instead, once I've got that working.
	 */
	private static final long      serialVersionUID = 1L;

	protected BoundingBox          area;

	// HashMap backing and easy persistence - need an extra list for this right
	// now.
	protected ArrayList<BlockData> blockList;
	protected HashSet<Long>        blockIdMap;

	protected int                  passesRemaining  = 1;
	protected int                  timeToLive       = 0;
	
	protected int				   taskId           = 0;
	
	protected static Map<Long, BlockData> modified = new HashMap<Long, BlockData>();

	public BlockList()
	{

	}

	public BlockList(BlockList other)
	{
		for (BlockData block : other)
		{
			BlockData newBlock = new BlockData(block);
			add(newBlock);
		}
		timeToLive = other.timeToLive;
		passesRemaining = other.passesRemaining;
	}

	public boolean add(Block block)
	{
		if (contains(block))
		{
			return true;
		}
		BlockData newBlock = new BlockData(block);
		return add(newBlock);
	}
	
	public boolean add(BlockData blockData)
	{
		// First do a sanity check with the map
		// Currently, we don't replace blocks!
		if (contains(blockData)) return true;
			
		if (blockIdMap == null)
		{
			blockIdMap = new HashSet<Long>();
		}

		if (blockList == null)
		{
			blockList = new ArrayList<BlockData>();
		}
		BlockVector blockLocation = blockData.getPosition();

		if (area == null)
		{
			area = new BoundingBox(blockLocation, blockLocation);
		}
		else
		{
			area = area.contain(blockLocation);
		}

		blockIdMap.add(blockData.getId());
		return blockList.add(blockData);
	}

	public boolean addAll(Collection<? extends BlockData> blocks)
	{
		// Iterate to maintain BB area
		boolean added = true;
		for (BlockData block : blocks)
		{
			added = added && add(block);
		}
		return added;
	}

	public void clear()
	{
		if (blockList == null)
		{
			return;
		}
		blockList.clear();
	}

	public boolean contains(Block block)
	{
		if (blockIdMap == null) return false;
		return blockIdMap.contains(BlockData.getBlockId(block));
	}

	public boolean contains(BlockData blockData)
	{
		if (blockIdMap == null || blockData == null)
		{
			return false;
		}

		return blockIdMap.contains(blockData.getId());
	}

	public boolean contains(Object arg0)
	{
		if (arg0 instanceof Block) {
			return contains((Block)arg0);
		}
		if (arg0 instanceof BlockData) {
			return contains((BlockData)arg0);
		}
		// Fall back to map
		return blockIdMap == null ? false : blockIdMap.contains(arg0);
	}

	public boolean containsAll(Collection<?> arg0)
	{
		if (blockIdMap == null)
		{
			return false;
		}
		return blockIdMap.containsAll(arg0);
	}

	// Collection interface- would be great if I could just extend HashSet and
	// have this "just work"

	// For now, this is here to keep the map up to date, and to pass through to
	// the blockList.

	public BoundingBox getArea()
	{
		return area;
	}

	public ArrayList<BlockData> getBlockList()
	{
		return blockList;
	}

	public boolean isEmpty()
	{
		if (blockList == null)
		{
			return true;
		}
		return blockList.isEmpty();
	}

	public Iterator<BlockData> iterator()
	{
		if (blockList == null)
		{
			return null;
		}
		return blockList.iterator();
	}

	public boolean remove(Object arg0)
	{
		// Note that we never shrink the BB!
		if (blockList == null)
		{
			return false;
		}
		return blockList.remove(arg0);
	}

	public boolean removeAll(Collection<?> arg0)
	{
		if (blockList == null)
		{
			return false;
		}
		return blockList.removeAll(arg0);
	}

	public boolean retainAll(Collection<?> arg0)
	{
		if (blockList == null)
		{
			return false;
		}
		return blockList.retainAll(arg0);
	}

	public void setArea(BoundingBox area)
	{
		this.area = area;
	}

	public void setBlockList(ArrayList<BlockData> blockList)
	{
		this.blockList = blockList;
		if (blockList != null)
		{
			blockIdMap = new HashSet<Long>();
			for (BlockData block : blockList)
			{
				blockIdMap.add(block.getId());
			}
		}
	}

	public void setRepetitions(int repeat)
	{
		passesRemaining = repeat;
	}

	public boolean isComplete()
	{
		return passesRemaining <= 0;
	}

	public void setTimeToLive(int ttl)
	{
		timeToLive = ttl;
	}

	public int getTimeToLive()
	{
		return timeToLive;
	}

	public int size()
	{
		if (blockList == null)
		{
			return 0;
		}
		return blockList.size();
	}

	public BlockData get(int index)
	{
		if (blockList == null || index >= blockList.size())
		{
			return null;
		}
		return blockList.get(index);
	}

	public Object[] toArray()
	{
		if (blockList == null)
		{
			return null;
		}
		return blockList.toArray();
	}

	public <T> T[] toArray(T[] arg0)
	{
		if (blockList == null)
		{
			return null;
		}
		return blockList.toArray(arg0);
	}
	
	public void prepareForUndo()
	{
		if (blockList == null) return;
		
		for (BlockData blockData : blockList) 
		{
			BlockData priorState = modified.get(blockData.getId());
			if (priorState != null)
			{
				priorState.setNextState(blockData);
				blockData.setPriorState(priorState);
			}

			modified.put(blockData.getId(), blockData);
		}
	}

	public void commit()
	{
		if (blockList == null) return;
		
		for (BlockData block : blockList)
		{
			BlockData currentState = modified.get(block.getId());
			if (currentState == block)
			{
				modified.remove(block.getId());
			}

			block.commit();
		}
	}
	
	public boolean undo(BlockData undoBlock)
	{
		if (undoBlock.undo()) {
			BlockData currentState = modified.get(undoBlock.getId());
			if (currentState == undoBlock) {
				modified.put(undoBlock.getId(), undoBlock.getPriorState());
			}
			
			undoBlock.undo();
			
			return true;
		}
		
		return false;
	}

	public boolean undo(Mage mage)
	{
		if (blockList == null) return true;

		UndoBatch batch = new UndoBatch(mage.getController(), this);
		if (!mage.addPendingBlockBatch(batch)) {
			return false;
		}
		passesRemaining--;
		
		return true;
	}
	
	public void load(ConfigurationSection node) {
		timeToLive = node.getInt("time_to_live", timeToLive);
		passesRemaining = node.getInt("passes_remaining", passesRemaining);
		List<String> blockData = node.getStringList("blocks");
		if (blockData != null) {
			for (String blockString : blockData) {
				add(BlockData.fromString(blockString));
			}
		}
	}
	
	public void save(Map<String, Object> dataMap) {
		dataMap.put("time_to_live", (Integer)timeToLive);
		dataMap.put("passes_remaining", (Integer)passesRemaining);
		List<String> blockData = new ArrayList<String>();
		if (blockList != null) {
			for (BlockData block : blockList) {
				blockData.add(block.toString());
			}
			dataMap.put("blocks", blockData);
		}
	}
	
	public String getWorldName() {
		if (blockList.size() == 0) return null;
		return blockList.get(0).getWorldName();
	}
	
	public void scheduleCleanup(Mage mage) {
		Plugin plugin = mage.getController().getPlugin();
		Server server = plugin.getServer();
		BukkitScheduler scheduler = server.getScheduler();

		// scheduler works in ticks- 20 ticks per second.
		long ticksToLive = timeToLive * 20 / 1000;
		taskId = scheduler.scheduleSyncDelayedTask(plugin, new CleanupBlocksTask(mage, this), ticksToLive);
	}
	
	public void onScheduledCleanup(Mage mage)
	{
		if (this.undo(mage)) {
			mage.getUndoQueue().removeScheduledCleanup(this);
		} else {
			// TODO: Retry limit?
			scheduleCleanup(mage);
		}
	}
	
	public boolean undoScheduled(Mage mage)
	{
		if (taskId > 0)
		{
			Plugin plugin = mage.getController().getPlugin();
			Server server = plugin.getServer();
			BukkitScheduler scheduler = server.getScheduler();
			scheduler.cancelTask(taskId);
			taskId = 0;
		}
		
		return this.undo(mage);
	}
}
