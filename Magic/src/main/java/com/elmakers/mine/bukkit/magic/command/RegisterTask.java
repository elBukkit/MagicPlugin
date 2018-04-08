package com.elmakers.mine.bukkit.magic.command;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class RegisterTask implements Runnable {
    private static final Object registerLock = new Object();

    private final Logger logger;
    private final File registerFile;
    private String playerId;
    private String playerName;
    private String code;

    public RegisterTask(Plugin plugin, String playerId, String playerName, String code) {
        logger = plugin.getLogger();
        File dataFolder = new File(plugin.getDataFolder(), "data");
        registerFile = new File(dataFolder, "registered.yml");
        this.playerId = playerId;
        this.playerName = playerName;
        this.code = code;
    }

    @Override
    public void run() {
        synchronized (registerLock) {
            register();
        }
    }

    private void register() {
        registerFile.getParentFile().mkdirs();
        YamlConfiguration registered = new YamlConfiguration();
        if (registerFile.exists()) {
            try {
                registered.load(registerFile);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error reading " + registerFile, ex);
                return;
            }
        }

        ConfigurationSection playerSection = registered.createSection(playerId);
        playerSection.set("name", playerName);
        playerSection.set("code", code);

        try {
            registered.save(registerFile);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error saving " + registerFile, ex);
            return;
        }
    }
}
