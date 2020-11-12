package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.wand.Wand;

public class DropActionTask implements Runnable {
    private final Wand wand;

    public DropActionTask(Wand wand) {
        this.wand = wand;
    }

    @Override
    public void run() {
        wand.performAction(wand.getDropAction());
    }
}
