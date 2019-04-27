package com.elmakers.mine.bukkit.magic;

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
