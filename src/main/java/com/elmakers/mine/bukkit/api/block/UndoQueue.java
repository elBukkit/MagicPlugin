package com.elmakers.mine.bukkit.api.block;

import org.bukkit.block.Block;

public interface UndoQueue {
	public void add(UndoList blocks);
	public void removeScheduledCleanup(UndoList blocks);
	public UndoList getLast();
	public UndoList getLast(Block target);
}
