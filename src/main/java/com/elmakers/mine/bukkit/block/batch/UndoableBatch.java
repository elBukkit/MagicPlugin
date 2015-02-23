package com.elmakers.mine.bukkit.block.batch;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.block.BlockBatch;
import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.UndoList;

public abstract class UndoableBatch implements BlockBatch {
    protected final MageController controller;
    protected final UndoList undoList;
    protected final Mage mage;
    protected boolean finished = false;

    public UndoableBatch(Mage mage) {
        this(mage, null);
    }

    public UndoableBatch(Mage mage, UndoList undoList) {
        this.controller = mage.getController();
        this.mage = mage;
        this.undoList = undoList == null ?  new UndoList(mage, null, "Undo") : undoList;
        mage.registerForUndo(this.undoList);
    }

    public void registerForUndo(Block block) {
        undoList.add(block, true);
    }

    public void registerForUndo(Entity entity) {
        undoList.add(entity);
    }

    public void finish() {
        if (!finished) {
            finished = true;
            controller.update(undoList);
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
