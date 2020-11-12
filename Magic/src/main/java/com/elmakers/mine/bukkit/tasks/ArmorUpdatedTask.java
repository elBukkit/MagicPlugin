package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.Mage;

public class ArmorUpdatedTask implements Runnable {
    private final Mage mage;

    public ArmorUpdatedTask(Mage mage) {
        this.mage = mage;
    }

    @Override
    public void run() {
        mage.armorUpdated();
    }
}
