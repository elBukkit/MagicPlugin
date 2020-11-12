package com.elmakers.mine.bukkit.tasks;

import java.util.Collection;

import com.elmakers.mine.bukkit.data.YamlDataFile;
import com.elmakers.mine.bukkit.magic.MagicController;

public class SaveDataTask implements Runnable {
    private final MagicController controller;
    private final Collection<YamlDataFile> data;

    public SaveDataTask(MagicController controller, Collection<YamlDataFile> data) {
        this.controller = controller;
        this.data = data;
    }

    @Override
    public void run() {
        controller.saveData(data);
    }
}
