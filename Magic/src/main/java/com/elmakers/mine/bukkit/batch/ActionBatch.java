package com.elmakers.mine.bukkit.batch;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.spell.Spell;

public class ActionBatch implements com.elmakers.mine.bukkit.api.batch.SpellBatch {
    private final int actionCount;
    private final CastContext context;
    private final ActionHandler handler;
    private boolean finished = false;

    public ActionBatch(CastContext context, ActionHandler handler) {
        this.context = context;
        this.handler = handler;
        this.actionCount = handler.getActionCount();
        if (handler.isUndoable()) {
            UndoList undoList = context.getUndoList();
            if (undoList != null) {
                undoList.setBatch(this);
            }
        }
    }

    @Override
    public int process(int maxBlocks) {
        if (finished) return 0;
        Spell spell = context.getSpell();
        if (spell.cancelOnNoPermission() && !context.canContinue(context.getLocation())) {
            cancel();
            return 0;
        }
        context.setWorkAllowed(maxBlocks);
        handler.perform(context);
        if (handler.isFinished() && !context.hasHandlers()) {
            finish();
        }
        return maxBlocks - context.getWorkAllowed();
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void finish() {
        if (!finished) {
            handler.cancel(context);
            handler.finish(context);
            context.finish();

            // Shouldn't need this anymore
            UndoList undoList = context.getUndoList();
            if (undoList != null) {
                undoList.setBatch(null);
            }

            finished = true;
        }
    }

    @Override
    public void cancel() {
        context.cancelEffects();
        context.getSpell().cancel();
        finish();
    }

    @Override
    public int size() {
        return actionCount;
    }

    @Override
    public int remaining() {
        return Math.max(0, actionCount - context.getActionsPerformed());
    }

    @Override
    @Nullable
    public Spell getSpell() {
        return context.getSpell();
    }

    @Override
    public String getName() {
        Spell spell = getSpell();
        if (spell == null) return "Unknown";
        return spell.getName();
    }

    @Override
    public UndoList getUndoList() {
        return context.getUndoList();
    }
}
