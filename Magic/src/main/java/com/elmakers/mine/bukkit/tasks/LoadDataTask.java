package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.MagicController;

public class LoadDataTask implements Runnable {
    private final MagicController controller;

    public LoadDataTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.finishLoadData();
    }
}
