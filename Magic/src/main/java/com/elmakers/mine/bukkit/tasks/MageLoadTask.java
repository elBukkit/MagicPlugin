package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.magic.Mage;

public class MageLoadTask implements Runnable {
    private final Mage mage;
    private final MageData data;

    public MageLoadTask(Mage mage, MageData data) {
        this.mage = mage;
        this.data = data;
    }

    @Override
    public void run() {
        mage.load(data);
    }
}
