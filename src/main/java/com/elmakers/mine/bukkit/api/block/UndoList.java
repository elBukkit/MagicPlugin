package com.elmakers.mine.bukkit.api.block;

import com.elmakers.mine.bukkit.api.magic.Mage;

public interface UndoList extends BlockList {
	public void commit();
	
	public void scheduleCleanup(Mage mage);
	public boolean undoScheduled(Mage mage);
	public boolean undo(Mage mage);
	
	public void setScheduleUndo(int ttl);
	public int getScheduledUndo();
}
