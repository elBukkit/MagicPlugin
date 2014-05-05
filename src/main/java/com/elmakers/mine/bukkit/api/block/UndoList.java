package com.elmakers.mine.bukkit.api.block;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.Mage;

public interface UndoList extends BlockList {
	public void commit();
	
	public void scheduleCleanup(Mage mage);
	public boolean undoScheduled(Mage mage);
	public boolean undo(Mage mage);
	
	public void setScheduleUndo(int ttl);
	public int getScheduledUndo();
	public boolean bypass();
	public long getCreatedTime();
	public long getModifiedTime();

	public void prune();
	
	public void add(Entity entity);
	public void remove(Entity entity);
	public void modify(Entity entity);
	
	public void convert(Entity entity, Block block);
	public void fall(Entity entity, Block block);
	public void explode(Entity entity, List<Block> explodedBlocks);
	public void cancelExplosion(Entity entity);
	
	public boolean contains(Location location, int threshold);
	
	public String getName();
	public Mage getOwner();
}
