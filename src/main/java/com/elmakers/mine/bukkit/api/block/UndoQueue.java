package com.elmakers.mine.bukkit.api.block;

import org.bukkit.block.Block;

public interface UndoQueue {
	public void add(BlockList blocks);
	public void removeScheduledCleanup(BlockList blocks);
	public BlockList getLast();
	public BlockList getLast(Block target);
}
