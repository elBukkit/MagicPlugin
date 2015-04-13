package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.utility.TimedRunnable;

public class UndoUpdateTask extends TimedRunnable {
    private final MagicController controller;

    public UndoUpdateTask(MagicController controller) {
        super("Rollback Scheduler");
        this.controller = controller;
    }

    @Override
    public void onRun() {
        controller.processUndo();
    }
}