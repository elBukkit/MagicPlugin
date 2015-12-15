package com.elmakers.mine.bukkit.magic;

import java.util.Collection;
import java.util.logging.Level;

public class MageUpdateTask implements Runnable {
    private final MagicController controller;

    public MageUpdateTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.forgetMages();
        Collection<com.elmakers.mine.bukkit.api.magic.Mage> mages = controller.getMages();
        for (com.elmakers.mine.bukkit.api.magic.Mage mage : mages) {
            if (!controller.isValid(mage)) {
                continue;
            }
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                try {
                    ((com.elmakers.mine.bukkit.magic.Mage) mage).tick();
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Error ticking Mage " + mage.getName(), ex);
                }
            }
        }
    }
}
