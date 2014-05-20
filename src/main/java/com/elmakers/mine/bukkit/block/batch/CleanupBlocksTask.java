package com.elmakers.mine.bukkit.block.batch;

import com.elmakers.mine.bukkit.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;

public class CleanupBlocksTask implements Runnable
{
    protected Mage mage;
    protected UndoList undoBlocks;

    public CleanupBlocksTask(Mage mage, UndoList cleanup)
    {
        this.undoBlocks = cleanup;
        this.mage = mage;
    }

    public void run()
    {
        if (undoBlocks.undo()) {
            mage.getUndoQueue().removeScheduledCleanup(undoBlocks);
        } else {
            // TODO: Retry limit?
            undoBlocks.scheduleCleanup();
        }
    }
}
