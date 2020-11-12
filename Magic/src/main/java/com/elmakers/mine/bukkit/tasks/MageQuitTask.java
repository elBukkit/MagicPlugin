package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.api.data.MageDataCallback;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;

public class MageQuitTask implements Runnable {
    private final MagicController controller;
    private final Mage mage;
    private final MageDataCallback callback;
    private final boolean isWandInventoryOpen;

    public MageQuitTask(MagicController controller, Mage mage, MageDataCallback callback, boolean isWandInventoryOpen) {
        this.controller = controller;
        this.mage = mage;
        this.isWandInventoryOpen = isWandInventoryOpen;
        this.callback = callback;
    }

    @Override
    public void run() {
        // Just in case the player relogged in that one tick..
        if (mage.isUnloading()) {
            controller.finalizeMageQuit(mage, callback, isWandInventoryOpen);
        }
    }
}
