package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.magic.Mage;

public class CheckWandTask implements Runnable {
    private final Mage mage;
    private final boolean checkAll;

    public CheckWandTask(Mage mage) {
        this.mage = mage;
        this.checkAll = false;
    }

    public CheckWandTask(Mage mage, boolean checkAll) {
        this.mage = mage;
        this.checkAll = checkAll;
    }

    @Override
    public void run() {
        mage.checkWand();
        if (checkAll) {
            mage.armorUpdated();
        }
    }
}
