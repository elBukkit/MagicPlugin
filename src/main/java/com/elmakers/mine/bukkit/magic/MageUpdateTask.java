package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.utility.TimedRunnable;

import java.util.Collection;
import java.util.logging.Level;

public class MageUpdateTask extends TimedRunnable {
    private final MagicController controller;

    public MageUpdateTask(MagicController controller) {
        super("Mage Tick");
        this.controller = controller;
    }

    @Override
    public void onRun() {
        controller.forgetMages();
        Collection<com.elmakers.mine.bukkit.api.magic.Mage> mages = controller.getMages();
        for (com.elmakers.mine.bukkit.api.magic.Mage mage : mages) {
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
