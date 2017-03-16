package com.elmakers.mine.bukkit.magic;

public class CheckWandTask implements Runnable {
    private final Mage mage;

    public CheckWandTask(Mage mage) {
        this.mage = mage;
    }

    @Override
    public void run() {
        mage.checkWand();
    }
}
