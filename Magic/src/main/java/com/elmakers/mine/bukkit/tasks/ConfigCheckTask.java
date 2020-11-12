package com.elmakers.mine.bukkit.tasks;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;

public class ConfigCheckTask implements Runnable {
    private final MagicController controller;
    private final File configCheckFile;

    private Long lastModified = null;

    public ConfigCheckTask(MagicController controller) {
        this.controller = controller;
        this.configCheckFile = new File(controller.getPlugin().getDataFolder(), "data/updated.yml");
    }

    @Override
    public void run() {
        if (configCheckFile.exists()) {
            long modified = configCheckFile.lastModified();
            UUID modifiedUserId = null;
            if (lastModified != null && modified > lastModified) {
                try {
                    YamlConfiguration updated = new YamlConfiguration();
                    updated.load(configCheckFile);
                    String userId = updated.getString("user_id");
                    if (userId != null && !userId.isEmpty()) {
                        modifiedUserId = UUID.fromString(userId);
                    }
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Error reading update file", ex);
                }

                final Player player = modifiedUserId == null ? null : Bukkit.getPlayer(modifiedUserId);

                // Don't reload if the modifying player is not online
                if (player != null || modifiedUserId == null) {
                    controller.getLogger().info("Config check file modified, reloading configuration");
                    final Mage mage = player == null ? null : controller.getRegisteredMage(player);
                    if (mage != null) {
                        controller.setReloadingMage(mage);
                    }
                    Bukkit.getScheduler().runTask(controller.getPlugin(), new ReloadConfigurationTask(controller));
                }
            }
            lastModified = modified;
        } else {
            lastModified = 0L;
        }
    }
}
