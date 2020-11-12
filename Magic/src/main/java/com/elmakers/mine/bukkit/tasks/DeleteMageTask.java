package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.api.magic.Mage;

public class DeleteMageTask implements Runnable {
    private final Mage mage;

    public DeleteMageTask(Mage mage) {
        this.mage = mage;
    }

    @Override
    public void run() {
        mage.getController().deleteMage(mage.getId());
    }
}
