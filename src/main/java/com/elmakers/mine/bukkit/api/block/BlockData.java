package com.elmakers.mine.bukkit.api.block;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

public interface BlockData extends MaterialAndData {
	public long getId();
	public World getWorld();
	public String getWorldName();
	public Block getBlock();
	public BlockVector getPosition();
	public void restore();
	public void commit();
	public boolean undo();

	public BlockData getNextState();
	public void setNextState(BlockData next);
	public BlockData getPriorState();
	public void setPriorState(BlockData prior);
}
