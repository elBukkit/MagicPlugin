package com.elmakers.mine.bukkit.integration.valhalla;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageClassTemplate;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.skills.skills.Skill;

public class MagicSkill extends Skill {
    private final MageController controller;
    private final String id;
    private final int priority;
    private final String mageClass;

    public MagicSkill(MageController controller, String skillId, String mageClass, int priority) {
        super(skillId.toUpperCase());
        this.controller = controller;
        this.priority = priority;
        this.mageClass = mageClass;
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
                } catch (Exception ex) {
                    controller.getLogger().log(Level.SEVERE, "Error loading: " + defaultsFileName + " from builtin resources", ex);
                }
            }

            if (fileType.equals("progression")) {
                addDefaultProgression(config);
            }

            try {
                config.save(configFile);
            } catch (Exception ex) {
                controller.getLogger().log(Level.SEVERE, "Error saving: " + defaultsFileName, ex);
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

    protected void addDefaultProgression(ConfigurationSection config) {
        MageClassTemplate mageClass = controller.getMageClassTemplate(this.mageClass);
        if (mageClass == null) {
            controller.getLogger().log(Level.WARNING, "Invalid mage class in Valhalla profile configs: " + this.mageClass);
            return;
        }
        String defaultPathId = mageClass.getString("path");
        ProgressionPath path = controller.getPath(defaultPathId);
        if (path == null) {
            controller.getLogger().log(Level.WARNING, "Invalid progression path via Valhalla profile configs: " + this.mageClass);
            return;
        }

        ConfigurationSection perks = config.createSection("perks");
        Collection<String> spells = path.getSpells();
        int xLocation = 1;
        int yLocation = 2;
        for (String spellKey : spells) {
            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            if (spell == null) continue;
            ConfigurationSection spellConfig = perks.createSection(spellKey);
            MaterialAndData spellIcon = spell.getIcon();
            Material material = spellIcon.getMaterial();
            int customModelData = spellIcon.getCustomModelData();
            String icon = material.name();
            if (customModelData > 0) {
                icon += ":" + customModelData;
            }
            spellConfig.set("icon", icon);
            spellConfig.set("name", spell.getName());
            spellConfig.set("description", spell.getDescription());
            spellConfig.set("cost", 1);
            spellConfig.set("coords", xLocation + "," + yLocation);
            xLocation++;
        }
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
