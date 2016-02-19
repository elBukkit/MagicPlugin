package com.elmakers.mine.bukkit.batch;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.UndoList;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class UndoBatch implements com.elmakers.mine.bukkit.api.batch.UndoBatch {
    protected final MageController controller;
    protected boolean finished = false;
    protected boolean applyPhysics = false;
    protected UndoList undoList;
    protected int listSize;
    protected int listProcessed;
    protected double partialWork = 0;

    private final Set<Material> attachables;

    public UndoBatch(UndoList blockList) {
        Mage mage = blockList.getOwner();
        controller = mage.getController();

        undoList = blockList;
        this.applyPhysics = blockList.getApplyPhysics();
        this.attachables = new HashSet<Material>();

        CastContext context = undoList.getContext();
        if (context != null) {
            context.playEffects("undo");
        }

        Set<Material> addToAttachables = controller.getMaterialSet("attachable");
        if (addToAttachables != null) {
            attachables.addAll(addToAttachables);
        }
        addToAttachables = controller.getMaterialSet("attachable_wall");
        if (addToAttachables != null) {
            attachables.addAll(addToAttachables);
        }
        addToAttachables = controller.getMaterialSet("attachable_double");
        if (addToAttachables != null) {
            attachables.addAll(addToAttachables);
        }
        addToAttachables = controller.getMaterialSet("delayed");
        if (addToAttachables != null) {
            attachables.addAll(addToAttachables);
        }

        // Sort so attachable items don't break
        undoList.sort(attachables);
        listSize = undoList.size();
    }

    public int size() {
        return listSize;
    }

    public int remaining() {
        return undoList == null ? 0 : undoList.size();
    }

    public int process(int maxBlocks) {
        int processedBlocks = 0;
        double undoSpeed = undoList.getUndoSpeed();
        if (undoSpeed > 0 && listProcessed < listSize) {
            partialWork += undoSpeed;
            if (partialWork > 1) {
                maxBlocks = (int)Math.floor(partialWork);
                partialWork = partialWork - maxBlocks;
            } else {
                return 0;
            }
        }
        while (undoList.size() > 0 && processedBlocks < maxBlocks) {
            BlockData undone = undoList.undoNext(applyPhysics);
            if (undone == null) {
                break;
            }
            processedBlocks++;
            listProcessed++;
        }
        if (undoList.size() == 0) {
            finish();
        }

        return processedBlocks;
    }

    public void finish() {
        if (!finished) {
            finished = true;
            undoList.unregisterAttached();
            undoList.undoEntityEffects();
            if (!undoList.isScheduled()) {
                controller.update(undoList);
            }
            CastContext context = undoList.getContext();
            if (context != null) {
                context.playEffects("undo_finished");
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
}
