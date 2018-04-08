package com.elmakers.mine.bukkit.magic;

import java.io.File;

import org.bukkit.Bukkit;

public class ConfigCheckTask implements Runnable {
    private final MagicController controller;
    private final File configCheckFile;

    private long lastModified = 0;

    public ConfigCheckTask(MagicController controller, String configCheckFile) {
        this.controller = controller;
        this.configCheckFile = new File(controller.getPlugin().getDataFolder(), configCheckFile);
    }

    @Override
    public void run() {
        if (configCheckFile.exists()) {
            long modified = configCheckFile.lastModified();
            if (lastModified != 0 && modified > lastModified) {
                controller.getLogger().info("Config check file modified, reloading configuration");
                controller.loadConfiguration(Bukkit.getConsoleSender());
            }
            lastModified = modified;
        }
    }
}
