package com.elmakers.mine.bukkit.tasks;

import java.util.Collection;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.magic.MagicController;

public class SaveMageDataTask implements Runnable {
    private final MagicController controller;
    private final Collection<MageData> mages;

    public SaveMageDataTask(MagicController controller, Collection<MageData> mages) {
        this.controller = controller;
        this.mages = mages;
    }

    @Override
    public void run() {
        controller.persistMageData(mages);
    }
}
