package com.elmakers.mine.bukkit.tasks;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.data.MageDataCallback;
import com.elmakers.mine.bukkit.magic.MagicController;

public class SaveMageTask implements Runnable {
    private final MagicController controller;
    private final MageData mageData;
    private final MageDataCallback callback;
    private final boolean releaseLock;

    public SaveMageTask(MagicController controller, MageData mageData, MageDataCallback callback, boolean releaseLock) {
        this.controller = controller;
        this.mageData = mageData;
        this.callback = callback;
        this.releaseLock = releaseLock;
    }

    @Override
    public void run() {
        controller.doSaveMage(mageData, callback, releaseLock);
    }
}
