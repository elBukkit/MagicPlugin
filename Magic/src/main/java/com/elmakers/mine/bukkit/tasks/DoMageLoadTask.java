package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;

public class DoMageLoadTask implements Runnable {
    private final MagicController controller;
    private final Mage mage;

    public DoMageLoadTask(MagicController controller, Mage mage) {
        this.mage = mage;
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.doSynchronizedLoadData(mage);
    }
}
