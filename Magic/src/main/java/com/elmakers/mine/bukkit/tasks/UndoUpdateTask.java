package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class UndoUpdateTask implements Runnable {
    private final MagicController controller;

    public UndoUpdateTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.processUndo();
    }
}
