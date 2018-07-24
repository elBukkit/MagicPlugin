package com.elmakers.mine.bukkit.batch;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.batch.Batch;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;

public abstract class UndoableBatch implements Batch {
    protected final MageController controller;
    protected final UndoList undoList;
    protected final Mage mage;
    protected boolean finished = false;

    public UndoableBatch(Mage mage, UndoList undoList) {
        this.controller = mage.getController();
        this.mage = mage;
        this.undoList = undoList;
        undoList.setBatch(this);
        mage.prepareForUndo(this.undoList);
    }

    public void registerForUndo(Block block) {
        undoList.add(block);
    }

    public void registerForUndo(Entity entity) {
        undoList.add(entity);
    }

    @Override
    public void finish() {
        if (!finished) {
            finished = true;
            if (!undoList.isScheduled()) {
                controller.update(undoList);
            }
            // Let GC collect the batch
            undoList.setBatch(null);
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
