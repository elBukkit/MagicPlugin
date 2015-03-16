package com.elmakers.mine.bukkit.batch;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.batch.Batch;

public class ActionBatch implements Batch {
    private final int actionCount;
    private final CastContext context;
    private final ActionHandler handler;

    public ActionBatch(CastContext context, ActionHandler handler) {
        this.context = context;
        this.handler = (ActionHandler)handler.clone();
        this.actionCount = handler.getActionCount();
    }

    @Override
    public int process(int maxBlocks) {
        context.setWorkAllowed(maxBlocks);
        handler.perform(context);
        if (handler.isFinished()) {
            handler.finish(context);
        }
        return maxBlocks - context.getWorkAllowed();
    }

    @Override
    public boolean isFinished() {
        return handler.isFinished();
    }

    @Override
    public void finish() {
        handler.finish(context);
    }

    @Override
    public int size() {
        return actionCount;
    }

    @Override
    public int remaining() {
        return Math.max(0, actionCount - context.getActionsPerformed());
    }
}
