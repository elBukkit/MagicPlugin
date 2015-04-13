package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.utility.TimedRunnable;

import java.util.Collection;
import java.util.logging.Level;

public class BlockUpdateTask extends TimedRunnable {
    private final MagicController controller;

    public BlockUpdateTask(MagicController controller) {
        super("Block Updates");
        this.controller = controller;
    }

    @Override
    public void onRun() {
        controller.processBlockUpdates();
    }
}