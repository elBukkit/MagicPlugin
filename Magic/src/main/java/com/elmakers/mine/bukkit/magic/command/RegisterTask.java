package com.elmakers.mine.bukkit.magic.command;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.utility.platform.SkinUtils;

public class RegisterTask implements Runnable {
    private static final Object registerLock = new Object();

    private final Logger logger;
    private final File registerFile;
    private final String playerId;
    private final String playerName;
    private final String code;
    private final String skinURL;

    public RegisterTask(Plugin plugin, Player player, String code) {
        logger = plugin.getLogger();
        File dataFolder = new File(plugin.getDataFolder(), "data");
        registerFile = new File(dataFolder, "registered.yml");

        playerId = player.getUniqueId().toString();
        playerName = player.getName();
        skinURL = SkinUtils.getOnlineSkinURL(playerName);
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
        playerSection.set("skin_url", skinURL);

        try {
            registered.save(registerFile);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error saving " + registerFile, ex);
            return;
        }
    }
}
