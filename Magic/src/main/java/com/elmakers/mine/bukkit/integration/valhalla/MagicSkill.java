package com.elmakers.mine.bukkit.integration.valhalla;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MageController;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.skills.skills.Skill;

public class MagicSkill extends Skill {
    private final MageController controller;
    private final String id;
    private final int priority;

    public MagicSkill(MageController controller, String skillId, int priority) {
        super(skillId.toUpperCase());
        this.controller = controller;
        this.priority = priority;
        id = skillId;
    }

    protected YamlConfiguration loadConfig(String fileType) {
        String fileSuffix = fileType.equals("skill") ? "" : "_" + fileType;
        ValhallaMMO valhallaPlugin = ValhallaMMO.getInstance();
        Plugin plugin = controller.getPlugin();
        File dataFolder = valhallaPlugin.getDataFolder();
        YamlConfiguration config = new YamlConfiguration();
        File configFile = new File(dataFolder, "skills/" + id + fileSuffix + ".yml");
        if (!configFile.exists()) {
            controller.getLogger().info("Creating default Magic Valhalla skill configuration at " + configFile.getAbsolutePath());
            String defaultsFileName = "examples/valhalla/" + fileType + ".yml";
            InputStream input = plugin.getResource(defaultsFileName);
            if (input != null)  {
                try {
                    config.load(new InputStreamReader(input, StandardCharsets.UTF_8.name()));
                    config.save(configFile);
                } catch (Exception ex) {
                    controller.getLogger().log(Level.SEVERE, "Error loading: " + defaultsFileName + " from builtin resources", ex);
                }
            }
        } else {
            try {
                config.load(configFile);
            } catch (Exception ex) {
                controller.getLogger().log(Level.SEVERE, "Error loading: " + configFile, ex);
            }
        }

        return config;
    }

    @Override
    public void loadConfiguration() {
        YamlConfiguration skillConfig = loadConfig("skill");
        YamlConfiguration progressionConfig = loadConfig("progression");
        loadCommonConfig(skillConfig, progressionConfig);
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return MagicProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return priority;
    }
}
