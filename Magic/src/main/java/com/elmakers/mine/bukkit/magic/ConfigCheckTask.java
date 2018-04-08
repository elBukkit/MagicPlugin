package com.elmakers.mine.bukkit.magic;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class ConfigCheckTask implements Runnable {
    private final MagicController controller;
    private final File configCheckFile;

    private long lastModified = 0;

    public static class Modified {
        public String userId;
    }

    public ConfigCheckTask(MagicController controller, String configCheckFile) {
        this.controller = controller;
        this.configCheckFile = new File(controller.getPlugin().getDataFolder(), configCheckFile);
    }

    @Override
    public void run() {
        if (configCheckFile.exists()) {
            long modified = configCheckFile.lastModified();
            UUID modifiedUserId = null;
            if (lastModified != 0 && modified > lastModified) {
                controller.getLogger().info("Config check file modified, reloading configuration");
                try {
                    Gson gson = new Gson();
                    JsonReader reader = new JsonReader(Files.newBufferedReader(configCheckFile.toPath(), StandardCharsets.UTF_8));
                    Modified modifiedData = gson.fromJson(reader, Modified.class);
                    if (modifiedData.userId != null && !modifiedData.userId.isEmpty()) {
                        modifiedUserId = UUID.fromString(modifiedData.userId);
                    }
                } catch (Exception ex) {
                    controller.getLogger().log(Level.WARNING, "Error reading update file", ex);
                }
                controller.loadConfiguration(Bukkit.getConsoleSender(), true);

                // The updating player should get their new spells
                final Player player = modifiedUserId == null ? null : Bukkit.getPlayer(modifiedUserId);
                final Mage mage = player == null ? null : controller.getRegisteredMage(player);
                if (player != null) {
                    Bukkit.getScheduler().runTask(controller.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            if (!player.hasPermission("Magic.notify")) {
                                player.sendMessage(ChatColor.AQUA + "Spells reloaded.");
                            }
                            mage.deactivate();
                            mage.checkWand();
                        }
                    });
                }
            }
            lastModified = modified;
        }
    }
}
