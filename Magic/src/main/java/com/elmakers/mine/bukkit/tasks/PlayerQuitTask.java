package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;

public class PlayerQuitTask implements Runnable {
    private final MagicController controller;
    private final Mage mage;

    public PlayerQuitTask(MagicController controller, Mage mage) {
        this.mage = mage;
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.playerQuit(mage);
    }
}
