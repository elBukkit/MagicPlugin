package com.elmakers.mine.bukkit.batch;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.block.BlockState;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.block.UndoList;

public class UndoBatch implements com.elmakers.mine.bukkit.api.batch.UndoBatch {
    protected final MageController controller;
    protected boolean finished = false;
    protected boolean applyPhysics = false;
    protected boolean lockChunks = false;
    protected UndoList undoList;
    protected int listSize;
    protected int listProcessed;
    protected double partialWork = 0;
    private Set<Chunk> affectedChunks = null;

    private final MaterialSet attachables;

    public UndoBatch(UndoList blockList) {
        controller = blockList.getController();

        undoList = blockList;
        this.applyPhysics = blockList.getApplyPhysics();
        this.setLockChunks(blockList.getLockChunks());

        CastContext context = undoList.getContext();
        if (context != null) {
            context.playEffects("undo");
        }

        MaterialSetManager materialSets = controller.getMaterialSetManager();
        this.attachables = materialSets.getMaterialSet("all_attachable");

        // Sort so attachable items don't break
        undoList.sort(attachables);
        listSize = undoList.size();
    }

    @Override
    public int size() {
        return listSize;
    }

    @Override
    public int remaining() {
        return undoList == null ? 0 : undoList.size();
    }

    @Override
    public int process(int maxWork) {
        if (finished) return 0;

        int workPerformed = 0;
        double undoSpeed = undoList.getUndoSpeed();
        if (undoSpeed > 0 && listProcessed < listSize && maxWork >= 0) {
            partialWork += undoSpeed;
            if (partialWork > 1) {
                maxWork = (int)Math.floor(partialWork);
                partialWork = partialWork - maxWork;
            } else {
                return 0;
            }
        }
        while (undoList.size() > 0 && (workPerformed < maxWork || maxWork < 0)) {
            BlockState prior = null;
            if (!undoList.isScheduled()) {
                BlockData next = undoList.getBlockList().iterator().next();
                if (next != null) {
                    prior = next.getBlock().getState();
                }
            }
            BlockData undone = undoList.undoNext(applyPhysics);
            if (undone == null) {
                // There may have been a forced chunk load here
                workPerformed += 20;
                break;
            } else if (lockChunks) {
                Chunk chunk = undone.getChunk();
                if (affectedChunks.add(chunk)) {
                    controller.lockChunk(chunk);
                }
            }
            if (prior != null) {
                Mage mage = undoList.getOwner();
                if (mage != null) {
                    controller.logBlockChange(mage, prior, undone.getBlock().getState());
                }
            }
            workPerformed += 10;
            listProcessed++;
        }
        if (undoList.size() == 0) {
            finish();
        }

        return workPerformed;
    }

    @Override
    public void cancel() {
        finish();
    }

    @Override
    public void finish() {
        if (!finished) {
            finished = true;
            undoList.unregisterWatched();
            undoList.undoEntityEffects();
            undoList.finish();
            if (!undoList.isScheduled()) {
                controller.update(undoList);
            }
            CastContext context = undoList.getContext();
            if (context != null) {
                context.playEffects("undo_finished");
            }
            if (lockChunks) {
                for (Chunk chunk : affectedChunks) {
                    controller.unlockChunk(chunk);
                }
                affectedChunks.clear();
            }
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public String getName() {
        return "Undo " + undoList.getName();
    }

    @Override
    public UndoList getUndoList() {
        return undoList;
    }

    @Override
    public void complete() {
        while (!isFinished()) {
            process(-1);
        }
    }

    public void setLockChunks(boolean lockChunks) {
        this.lockChunks = lockChunks;
        if (lockChunks && affectedChunks == null) {
            affectedChunks = new HashSet<>();
        }
    }
}
