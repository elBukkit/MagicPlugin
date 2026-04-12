package com.elmakers.mine.bukkit.integration.valhalla;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final int levelsPerPath;
    private final String mageClass;

    public MagicSkill(MageController controller, String skillId, String mageClass, ConfigurationSection config) {
        super(skillId.toUpperCase());
        this.controller = controller;
        this.priority = config.getInt("priority");
        this.levelsPerPath = config.getInt("levels_per_path");
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
        List<String> defaultPerks = new ArrayList<>();
        defaultPerks.add(mageClass.getString("path"));
        Collection<String> defaultSpells = mageClass.getStringList("spells");
        defaultPerks.addAll(defaultSpells);
        ConfigurationSection startingPerks = config.createSection("starting_perks");
        startingPerks.set("perks_unlocked_add", defaultPerks);

        String defaultPathId = mageClass.getString("path");
        ProgressionPath path = controller.getPath(defaultPathId);
        if (path == null) {
            controller.getLogger().log(Level.WARNING, "Invalid progression path via Valhalla profile configs: " + this.mageClass);
            return;
        }

        int yLocation = 1;
        int levelUnlock = 0;
        Set<String> previousSpells = new HashSet<>();
        ConfigurationSection perks = config.createSection("perks");
        ProgressionPath previousPath = null;
        while (path != null) {
            addRankPerk(perks, yLocation, path, previousPath, levelUnlock);
            yLocation--;
            previousPath = path;
            addPathPerks(perks, yLocation, path, previousSpells);
            path = path.getNextPath();
            levelUnlock += levelsPerPath;
            yLocation--;
        }
    }

    protected void addRankPerk(ConfigurationSection perks, int yLocation, ProgressionPath path, ProgressionPath previousPath, int level) {
        int xLocation = 2;
        ConfigurationSection pathConfig = perks.createSection(path.getKey());
        MaterialAndData spellIcon = path.getIcon();
        Material material = spellIcon.getMaterial();
        int customModelData = spellIcon.getCustomModelData();
        MaterialAndData lockedIcon = path.getDisabledIcon();
        int disabledData = lockedIcon.getCustomModelData();
        if (disabledData == 0) {
            disabledData = customModelData;
        }
        if (customModelData > 0) {
            pathConfig.set("custom_model_data_unlockable", customModelData);
            pathConfig.set("custom_model_data_unlocked", customModelData);
            // Visible kind of means locked, since "unlockable" means not locked
            pathConfig.set("custom_model_data_visible", disabledData);
        }
        pathConfig.set("icon", material.name());
        pathConfig.set("name", path.getName());
        pathConfig.set("description", path.getDescription());
        pathConfig.set("cost", 0);
        pathConfig.set("coords", xLocation + "," + yLocation);
        pathConfig.set("required_lv", level);
        if (previousPath != null) {
            List<String> required = new ArrayList<>();
            required.add(previousPath.getKey());
            pathConfig.set("requireperk_all", required);
        }
    }

    protected void addPathPerks(ConfigurationSection perks, int yLocation, ProgressionPath path, Set<String> previousSpells) {
        Collection<String> spells = path.getSpells();
        Collection<String> newSpells = new HashSet<>();
        for (String spellKey : spells) {
            if (previousSpells.contains(spellKey)) continue;
            newSpells.add(spellKey);
        }

        int xLocation = -(int)Math.floor(newSpells.size() / 2) + 2;
        for (String spellKey : newSpells) {
            if (previousSpells.contains(spellKey)) continue;
            previousSpells.add(spellKey);
            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            if (spell == null) continue;
            ConfigurationSection spellConfig = perks.createSection(spellKey);
            MaterialAndData spellIcon = spell.getIcon();
            Material material = spellIcon.getMaterial();
            int customModelData = spellIcon.getCustomModelData();
            MaterialAndData lockedIcon = spell.getDisabledIcon();
            int disabledData = lockedIcon.getCustomModelData();
            if (disabledData == 0) {
                disabledData = customModelData;
            }
            if (customModelData > 0) {
                spellConfig.set("custom_model_data_unlockable", customModelData);
                spellConfig.set("custom_model_data_unlocked", customModelData);
                // Visible kind of means locked, since "unlockable" means not locked
                spellConfig.set("custom_model_data_visible", disabledData);
            }
            spellConfig.set("icon", material.name());
            spellConfig.set("name", spell.getName());
            spellConfig.set("description", spell.getDescription());
            spellConfig.set("cost", 1);
            spellConfig.set("coords", xLocation + "," + yLocation);
            List<String> required = new ArrayList<>();
            required.add(path.getKey());
            spellConfig.set("requireperk_all", required);
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
