package com.elmakers.mine.bukkit.block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.block.batch.CleanupBlocksTask;
import com.elmakers.mine.bukkit.block.batch.UndoBatch;
import com.elmakers.mine.bukkit.entity.EntityData;

/**
 * Implements a Collection of Blocks, for quick getting/putting while iterating
 * over a set or area of blocks.
 * 
 * This stores BlockData objects, which are hashable via their Persisted
 * inheritance, and their LocationData id (which itself has a hash function
 * based on world name and BlockVector's hash function)
 * 
 */
public class UndoList extends BlockList implements com.elmakers.mine.bukkit.api.block.UndoList
{
	protected Set<Entity> 		   	entities;
	protected Set<EntityData> 	   	removedEntities;
	protected final Plugin		   	plugin;

	protected int                  	passesRemaining  = 1;
	protected int                  	timeToLive       = 0;
	
	protected int				   	taskId           = 0;
	
	protected boolean				bypass		 	= false;
	protected final long			created;
	
	public UndoList(Plugin plugin)
	{
		this.plugin = plugin;
		created = System.currentTimeMillis();
	}

	public UndoList(UndoList other)
	{
		super(other);
		this.plugin = other.plugin;
		timeToLive = other.timeToLive;
		passesRemaining = other.passesRemaining;
		created = other.created;
	}

	@Override
	public int size()
	{
		return (
				(blockList == null ? 0 :blockList.size()) 
			+ 	(entities == null ? 0 : entities.size()));
	}

	@Override
	public boolean isEmpty()
	{
		return (
			(blockList == null || blockList.isEmpty()) 
		&& 	(entities == null || entities.isEmpty()));
	}

	public void setRepetitions(int repeat)
	{
		passesRemaining = repeat;
	}

	public boolean isComplete()
	{
		return passesRemaining <= 0;
	}

	public void setScheduleUndo(int ttl)
	{
		timeToLive = ttl;
	}

	public int getScheduledUndo()
	{
		return timeToLive;
	}
	
	@Override
	public boolean add(BlockData blockData)
	{
		if (!super.add(blockData)) return false;
		if (bypass) return true;
		
		BlockData priorState = modified.get(blockData.getId());
		if (priorState != null)
		{
			priorState.setNextState(blockData);
			blockData.setPriorState(priorState);
		}

		modified.put(blockData.getId(), blockData);
		return true;
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
			if (currentState == undoBlock) 
			{
				modified.put(undoBlock.getId(), undoBlock.getPriorState());
			}
			
			undoBlock.undo();
			
			return true;
		}
		
		return false;
	}

	public boolean undo(Mage mage)
	{
		// This part doesn't happen asynchronously
		if (entities != null) {
			for (Entity entity : entities) {
				entity.remove();
			}
			entities = null;
		}
		if (removedEntities != null) {
			for (EntityData entity : removedEntities) {
				entity.spawn();
			}
			removedEntities = null;
		}

		if (blockList == null) return true;

		UndoBatch batch = new UndoBatch(mage, this);
		if (!mage.addPendingBlockBatch(batch)) {
			return false;
		}
		passesRemaining--;
		
		return true;
	}
	
	@Override
	public void load(ConfigurationSection node)
	{
		super.load(node);
		timeToLive = node.getInt("time_to_live", timeToLive);
		passesRemaining = node.getInt("passes_remaining", passesRemaining);
	}
	
	@Override
	public void save(ConfigurationSection node) 
	{
		super.save(node);
		node.set("time_to_live", (Integer)timeToLive);
		node.set("passes_remaining", (Integer)passesRemaining);
	}
	
	public void scheduleCleanup(Mage mage) 
	{
		Server server = plugin.getServer();
		BukkitScheduler scheduler = server.getScheduler();

		// scheduler works in ticks- 20 ticks per second.
		long ticksToLive = timeToLive * 20 / 1000;
		taskId = scheduler.scheduleSyncDelayedTask(plugin, new CleanupBlocksTask(mage, this), ticksToLive);
	}
	
	public boolean undoScheduled(Mage mage)
	{
		if (taskId > 0)
		{
			Server server = plugin.getServer();
			BukkitScheduler scheduler = server.getScheduler();
			scheduler.cancelTask(taskId);
			taskId = 0;
		}
		
		return this.undo(mage);
	}
	
	public void watch(Entity entity)
	{
		if (entity == null) return;
		entity.setMetadata("MagicBlockList", new FixedMetadataValue(plugin, this));
	}
	
	public void add(Entity entity)
	{
		if (entities == null) entities = new HashSet<Entity>();
		if (worldName != null && !entity.getWorld().equals(worldName)) return;
		if (worldName == null) worldName = entity.getWorld().getName();
		
		entities.add(entity);
		watch(entity);
		contain(entity.getLocation().toVector());
	}
	
	public void addRemoved(Entity entity)
	{
		addRemoved(new EntityData(entity));
	}
	
	public void addRemoved(EntityData entity)
	{
		if (removedEntities == null) removedEntities = new HashSet<EntityData>();
		removedEntities.add(entity);
	}
	
	public void convert(Entity fallingBlock, Block block)
	{
		if (entities != null) {
			entities.remove(fallingBlock);
		}
		add(block);
	}
	
	public void fall(Entity fallingBlock, Block block)
	{
		add(fallingBlock);
		add(block);
	}
	
	public void explode(Entity explodingEntity, List<Block> blocks)
	{
		if (entities != null) {
			entities.remove(explodingEntity);
		}
		for (Block block : blocks) {
			add(block);
		}
	}
	
	public void cancelExplosion(Entity explodingEntity)
	{
		if (entities != null) {
			entities.remove(explodingEntity);
		}
	}
	
	public boolean bypass()
	{
		return bypass;
	}
	
	public void setBypass(boolean bypass)
	{
		this.bypass = bypass;
	}
	
	public long getCreatedTime()
	{
		return this.created;
	}
	
	public boolean contains(Location location, int threshold)
	{
		if (location == null || area == null || worldName == null) return false;
		if (!location.getWorld().getName().equals(worldName)) return false;
		
		return area.contains(location.toVector(), threshold);
	}
}
