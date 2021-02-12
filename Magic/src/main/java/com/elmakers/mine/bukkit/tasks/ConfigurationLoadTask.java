package com.elmakers.mine.bukkit.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.collect.ImmutableSet;

public class ConfigurationLoadTask implements Runnable {
    private final MagicController controller;
    private final File configFolder;
    private final Plugin plugin;
    private final CommandSender sender;
    private boolean verboseLogging;

    private static final String[] CONFIG_FILES = {"messages", "materials", "attributes", "effects", "spells", "paths",
            "classes", "wands", "items", "kits", "crafting", "mobs", "automata", "modifiers", "worlds"};
    private static final ImmutableSet<String> DEFAULT_ON = ImmutableSet.of("messages", "materials");

    private final Map<String, ConfigurationSection> loadedConfigurations = new HashMap<>();
    private final Map<String, ConfigurationSection> mainConfigurations = new HashMap<>();

    private final Map<String, ConfigurationSection> spellConfigurations     = new HashMap<>();
    private final Map<String, ConfigurationSection> baseSpellConfigurations = new HashMap<>();
    private final Map<String, ConfigurationSection> exampleConfigurations = new HashMap<>();
    private final Map<String, String> exampleKeyNames = new HashMap<>();

    private static final Object loadLock = new Object();

    private boolean allPvpRestricted = false;
    private boolean noPvpRestricted = false;
    private boolean saveDefaultConfigs = true;
    private boolean spellUpgradesEnabled = true;

    private String exampleDefaults = null;
    private String languageOverride = null;
    private Collection<String> addExamples = null;
    private Set<String> allExamples = new HashSet<>();

    private ConfigurationSection mainConfiguration;

    private Set<String> resolvingKeys = new LinkedHashSet<>();

    private boolean success;

    public ConfigurationLoadTask(MagicController controller, CommandSender sender) {
        this.controller = controller;
        this.sender = sender;
        plugin = controller.getPlugin();
        configFolder = controller.getConfigFolder();
    }

    private Logger getLogger() {
        return controller.getLogger();
    }

    private void loadInitialProperties(ConfigurationSection properties) {
        spellUpgradesEnabled = properties.getBoolean("enable_spell_upgrades", true);
        allPvpRestricted = properties.getBoolean("pvp_restricted", false);
        noPvpRestricted = properties.getBoolean("allow_pvp_restricted", false);
        saveDefaultConfigs = properties.getBoolean("save_default_configs", true);
        exampleDefaults = properties.getString("example", exampleDefaults);
        addExamples = ConfigurationUtils.getStringList(properties,"add_examples");
        if (addExamples == null || addExamples.isEmpty()) {
            addExamples = ConfigurationUtils.getStringList(properties,"examples");
        }
        languageOverride = properties.getString("language");
    }

    private void info(String message) {
        if (verboseLogging) {
            controller.info(message);
        }
    }

    public void setVerbose(boolean verbose) {
        this.verboseLogging = verbose;
    }

    @Nonnull
    private ConfigurationSection loadExampleConfiguration(String examplesPrefix, String exampleKey) {
        ConfigurationSection exampleConfig = exampleConfigurations.get(examplesPrefix);
        if (exampleConfig == null) {
            boolean isMainConfig = examplesPrefix.endsWith("config");
            String examplesFileName = examplesPrefix + ".yml";
            File externalFolder = new File(plugin.getDataFolder(), examplesPrefix);
            File externalFile = new File(plugin.getDataFolder(), examplesFileName);
            if (externalFile.exists()) {
                try {
                    exampleConfig = CompatibilityUtils.loadConfiguration(externalFile);
                } catch (Exception ex) {
                    getLogger().severe("Error loading: " + examplesFileName);
                }
            }
            if (externalFolder.exists()) {
                try {
                    if (exampleConfig == null) {
                        exampleConfig = ConfigurationUtils.newConfigurationSection();
                    }
                    exampleConfig = loadConfigFolder(exampleConfig, externalFolder, false);
                } catch (Exception ex) {
                    getLogger().severe("Error loading: " + examplesFileName);
                }
            }

            // Check for meta info
            if (isMainConfig) {
                File metaFile = new File(plugin.getDataFolder(), "examples/" + exampleKey + "/example.yml");
                if (metaFile.exists()) {
                    try {
                        YamlConfiguration exampleMetadata = new YamlConfiguration();
                        exampleMetadata.load(metaFile);
                        String name = exampleMetadata.getString("name", exampleKey);
                        if (!name.equalsIgnoreCase(exampleKey)) {
                            exampleKeyNames.put(exampleKey, name);
                        }
                    } catch (Exception ex) {
                        getLogger().severe("Error loading external example meta file: " + metaFile.getPath());
                    }
                }
            }
            if (exampleConfig == null) {
                InputStream input = plugin.getResource(examplesFileName);
                if (input != null)  {
                    try {
                        exampleConfig = CompatibilityUtils.loadConfiguration(input);
                    } catch (Exception ex) {
                        getLogger().severe("Error loading: " + examplesFileName + " from builtin resources");
                    }
                }
            }
            if (exampleConfig == null) {
                exampleConfig = ConfigurationUtils.newConfigurationSection();
            }
            exampleConfigurations.put(examplesPrefix, exampleConfig);
        }
        return ConfigurationUtils.cloneConfiguration(exampleConfig);
    }

    private ConfigurationSection loadMainConfiguration() throws InvalidConfigurationException, IOException {
        ConfigurationSection configuration = loadMainConfiguration("config");
        loadInitialProperties(configuration);
        boolean reloadConfig = false;
        if (addExamples != null && addExamples.size() > 0) {
            allExamples.addAll(addExamples);
            info("Adding examples: " + StringUtils.join(addExamples, ","));
            reloadConfig = true;
        }
        if (exampleDefaults != null && exampleDefaults.length() > 0) {
            allExamples.add(exampleDefaults);
            info("Overriding configuration with example: " + exampleDefaults);
            reloadConfig = true;
        }

        if (reloadConfig) {
            // Reload config, examples will be used this time.
            configuration = loadMainConfiguration("config");
        }

        return configuration;
    }

    private ConfigurationSection loadMainConfiguration(String fileName) throws InvalidConfigurationException, IOException {
        ConfigurationSection overrides = loadOverrides(fileName);

        // hack to make overriding the RP less confusing
        if (overrides != null && !overrides.contains("add_resource_pack")) {
            overrides.set("add_resource_pack", overrides.get("resource_pack"));
        }

        boolean usingExample = exampleDefaults != null && exampleDefaults.length() > 0;
        String examplesFilePrefix = usingExample ? "examples/" + exampleDefaults + "/" + fileName : null;
        String defaultsFileName = "defaults/" + fileName + ".defaults.yml";

        // Start with default configs
        YamlConfiguration config;
        try {
            config = CompatibilityUtils.loadConfiguration(plugin.getResource(defaultsFileName));
        } catch (Exception ex) {
            getLogger().severe("Error loading file: " + defaultsFileName);
            throw ex;
        }
        info(" Based on defaults " + defaultsFileName);

        // Load lists
        String listsFilename = "defaults/lists.defaults.yml";
        YamlConfiguration listConfig = null;
        try {
            listConfig = CompatibilityUtils.loadConfiguration(plugin.getResource(listsFilename));
            ConfigurationUtils.addConfigurations(config, listConfig);
        } catch (Exception ex) {
            getLogger().severe("Error loading file: " + listsFilename);
            throw ex;
        }
        info(" Added lists from " + listsFilename);

        // Load an example if one is specified
        if (usingExample) {
            ConfigurationSection exampleConfig = loadExampleConfiguration(examplesFilePrefix, exampleDefaults);
            if (exampleConfig != null)  {
                try {
                    info(" Using " + examplesFilePrefix);
                    processInheritance(exampleDefaults, exampleConfig, fileName, exampleConfig);
                    mainConfigurations.put(exampleDefaults, exampleConfig);
                    ConfigurationUtils.addConfigurations(config, exampleConfig);
                } catch (Exception ex) {
                    getLogger().severe("Error loading: " + examplesFilePrefix);
                    throw ex;
                }
            }
        }
        if (addExamples != null && addExamples.size() > 0) {
            for (String example : addExamples) {
                examplesFilePrefix = "examples/" + example + "/" + fileName;
                ConfigurationSection exampleConfig = loadExampleConfiguration(examplesFilePrefix, example);
                if (exampleConfig != null)
                {
                    try {
                        boolean override = exampleConfig.getBoolean("example_override", false);
                        info(" Adding " + examplesFilePrefix + (override ? ", allowing overrides" : ""));
                        processInheritance(example, exampleConfig, fileName, exampleConfig);
                        mainConfigurations.put(example, exampleConfig);
                        ConfigurationUtils.addConfigurations(config, exampleConfig, override);
                    } catch (Exception ex) {
                        getLogger().severe("Error loading: " + examplesFilePrefix);
                        throw ex;
                    }
                }
            }
        }

        // Apply version-specific configs
        addVersionConfigs(config, fileName);

        // Apply overrides after loading defaults and examples
        ConfigurationUtils.addConfigurations(config, overrides);

        // Apply file overrides last
        File configSubFolder = new File(configFolder, fileName);
        loadConfigFolder(config, configSubFolder, false);

        // Save default configs for inspection
        if (saveDefaultConfigs) {
            try {
                // For the main config file we just save the defaults directly, it has a
                // lot of comments that are useful to see.
                plugin.saveResource(defaultsFileName, true);
                plugin.saveResource(listsFilename, true);
            } catch (Exception ex) {
                getLogger().warning("Couldn't write defaults file: " + defaultsFileName);
            }
        } else  {
            deleteDefaults(defaultsFileName);
        }

        return config;
    }

    private void processInheritance(String exampleKey, ConfigurationSection exampleConfig, String fileName, ConfigurationSection mainConfiguration) {
        processInheritance(exampleKey, exampleConfig, fileName, mainConfiguration, null);
    }

    private void processInheritance(String exampleKey, ConfigurationSection exampleConfig, String fileName, ConfigurationSection mainConfiguration, Set<String> inherited) {
        // This lets a configuration be dropped into the plugins/Magic folder, or plugins/Magic/examples
        // and behave the same way.
        if (mainConfiguration.contains("example")) {
            mainConfiguration.set("inherit", mainConfiguration.get("example"));
            mainConfiguration.set("example", null);
        }

        // We can have multiple inheritance which causes us to actually load the same configuration
        // twice, so we have to traverse each tree edge individually
        boolean isMainConfig = fileName.equals("config") || fileName.equals("messages") || fileName.equals("materials");
        if (inherited == null) {
            inherited = new LinkedHashSet<>();
            if (!isMainConfig) {
                enableAll(exampleConfig);
            }
        } else {
            inherited = new LinkedHashSet<>(inherited);
        }
        inherited.add(exampleKey);
        List<String> inherits = ConfigurationUtils.getStringList(mainConfiguration, "inherit");
        if (inherits != null) {
            List<String> skip = ConfigurationUtils.getStringList(mainConfiguration, "skip_inherited");
            if (skip == null || !skip.contains(fileName)) {
                for (String inheritFrom : inherits) {
                    String inheritFilePrefix = "examples/" + inheritFrom + "/" + fileName;
                    ConfigurationSection inheritedConfig = loadExampleConfiguration(inheritFilePrefix, inheritFrom);
                    if (inheritedConfig != null) {
                        if (isMainConfig) {
                            mainConfigurations.put(inheritFrom, ConfigurationUtils.cloneConfiguration(inheritedConfig));

                            // These should not be inherited
                            inheritedConfig.set("disable_inherited", null);
                            inheritedConfig.set("skip_inherited", null);
                            inheritedConfig.set("example", null);
                            inheritedConfig.set("inherit", null);
                        }
                        try {
                            List<String> disable = ConfigurationUtils.getStringList(mainConfiguration, "disable_inherited");
                            if (inherited.contains(inheritFrom)) {
                                getLogger().log(Level.WARNING, "    Circular dependency detected in configuration inheritance: " + StringUtils.join(inherited, " -> ") + " -> " + inheritFrom);
                            } else {
                                processInheritance(inheritFrom, inheritedConfig, fileName, getMainConfiguration(inheritFrom), inherited);
                            }
                            if (!isMainConfig && disable != null && disable.contains(fileName)) {
                                disableAll(inheritedConfig);
                            }
                            ConfigurationUtils.addConfigurations(exampleConfig, inheritedConfig, false);
                            info("   Example " + exampleKey + " inheriting from " + inheritFrom);
                        } catch (Exception ex) {
                            getLogger().severe("Error loading file: " + inheritFilePrefix);
                            throw ex;
                        }
                    }
                }
            }
        }
        // Prepare this config to be merged with others that may have force-disabled some of this config via inheritance
        if (!isMainConfig) {
            enableAll(exampleConfig);
        }
    }

    private ConfigurationSection loadOverrides(String fileName) throws IOException, InvalidConfigurationException {
        String configFileName = fileName + ".yml";
        File configFile = new File(configFolder, configFileName);
        if (!configFile.exists()) {
            info("Saving template " + configFileName + ", edit to customize configuration.");
            plugin.saveResource(configFileName, false);
        }

        info("Loading " + configFile.getName());
        ConfigurationSection results;
        try {
            results = CompatibilityUtils.loadConfiguration(configFile);
        } catch (Exception ex) {
            getLogger().severe("Error loading: " + configFileName);
            throw ex;
        }
        return results;
    }

    private ConfigurationSection loadConfigFile(String fileName, ConfigurationSection mainConfiguration)
        throws IOException, InvalidConfigurationException {

        boolean loadAllDefaults = mainConfiguration.getBoolean("load_default_configs", true);
        // materials and messages are hard to turn off
        if (DEFAULT_ON.contains(fileName)) {
            loadAllDefaults = true;
        }
        boolean isMainConfig = fileName.equals("config") || fileName.equals("messages") || fileName.equals("materials");
        boolean loadDefaults = mainConfiguration.getBoolean("load_default_" + fileName, loadAllDefaults);
        boolean disableDefaults = mainConfiguration.getBoolean("disable_default_" + fileName, false);

        ConfigurationSection mainSection = mainConfiguration.getConfigurationSection(fileName);

        ConfigurationSection overrides = loadOverrides(fileName);
        boolean usingExample = exampleDefaults != null && exampleDefaults.length() > 0;

        String examplesFilePrefix = usingExample ? "examples/" + exampleDefaults + "/" + fileName : null;
        String defaultsFileName = "defaults/" + fileName + ".defaults.yml";

        YamlConfiguration config = new YamlConfiguration();

        YamlConfiguration defaultConfig = null;
        try {
            defaultConfig = CompatibilityUtils.loadConfiguration(plugin.getResource(defaultsFileName));
        } catch (Exception ex) {
            getLogger().severe("Error loading file: " + defaultsFileName);
            throw ex;
        }
        String header = defaultConfig.options().header();

        // Load defaults
        if (loadDefaults) {
            info(" Based on defaults " + defaultsFileName);
            if (disableDefaults) {
                disableAll(defaultConfig);
            }
            ConfigurationUtils.addConfigurations(config, defaultConfig);
        }

        // Load example
        if (usingExample && loadDefaults) {
            ConfigurationSection exampleConfig = loadExampleConfiguration(examplesFilePrefix, exampleDefaults);
            if (exampleConfig != null) {
                try {
                    if (disableDefaults) {
                        disableAll(exampleConfig);
                    }
                    processInheritance(exampleDefaults, exampleConfig, fileName, getMainConfiguration(exampleDefaults));
                    ConfigurationUtils.addConfigurations(config, exampleConfig);
                    info(" Using " + examplesFilePrefix);
                } catch (Exception ex) {
                    getLogger().severe("Error loading file: " + examplesFilePrefix);
                    throw ex;
                }
            }
        }

        // Load anything relevant from the main config
        if (mainSection != null) {
            ConfigurationUtils.addConfigurations(overrides, mainSection);
        }

        // Add in examples
        if (addExamples != null && addExamples.size() > 0) {
            for (String example : addExamples) {
                examplesFilePrefix = "examples/" + example + "/" + fileName;
                ConfigurationSection exampleConfig = loadExampleConfiguration(examplesFilePrefix, example);
                if (exampleConfig != null)
                {
                    try {
                        processInheritance(example, exampleConfig, fileName, getMainConfiguration(example));
                        reenableAll(config, exampleConfig);
                        ConfigurationUtils.addConfigurations(config, exampleConfig, false);
                        info(" Added " + examplesFilePrefix);
                    } catch (Exception ex) {
                        getLogger().severe("Error loading file: " + examplesFilePrefix);
                        throw ex;
                    }
                }
            }
        }

        // Apply version-specific configs
        addVersionConfigs(config, fileName);

        // Apply language overrides, but only to the messages config
        if (fileName.equals("messages") && languageOverride != null && !languageOverride.isEmpty() && !languageOverride.equalsIgnoreCase("EN")) {
            String languageFilePrefix = "examples/localizations/messages." + languageOverride;
            ConfigurationSection languageConfig = loadExampleConfiguration(languageFilePrefix, "localizations");
            if (languageConfig != null) {
                try {
                    ConfigurationUtils.addConfigurations(config, languageConfig);
                    info(" Using " + languageFilePrefix);
                } catch (Exception ex) {
                    getLogger().severe("Error loading file: " + languageFilePrefix);
                    throw ex;
                }
            }
        }

        // Apply overrides after loading defaults and examples
        if (isMainConfig) {
            enableAll(overrides);
        }
        ConfigurationUtils.addConfigurations(config, overrides);

        // Apply file overrides last
        File configSubFolder = new File(configFolder, fileName);
        loadConfigFolder(config, configSubFolder, !isMainConfig);

        // Clear any enabled flags we added in to re-enable disabled inherited configs
        clearEnabled(config);

        // Save defaults
        File savedDefaults = new File(configFolder, defaultsFileName);
        if (saveDefaultConfigs) {
            try {
                config.options().header(header);
                config.save(savedDefaults);
            } catch (Exception ex) {
                getLogger().warning("Couldn't write defaults file: " + defaultsFileName);
            }
        } else  {
            deleteDefaults(defaultsFileName);
        }

        return config;
    }

    private void addVersionConfigs(ConfigurationSection config, String fileName) throws InvalidConfigurationException, IOException {
        int[] serverVersion = CompatibilityUtils.getServerVersion();
        int majorVersion = serverVersion[0];
        int minorVersion = serverVersion[1];
        String versionExample = majorVersion + "." + minorVersion;
        String versionFileName = "examples/" + versionExample + "/" + fileName + ".yml";
        InputStream versionInput = plugin.getResource(versionFileName);
        if (versionInput != null)  {
            try {
                ConfigurationSection versionConfig = CompatibilityUtils.loadConfiguration(versionInput);
                // Version patches will never add to configs, the top-level nodes they are modifying must exist.
                // This allows them to tweak things from example configs but get safely ignored if not loading
                // those examples.
                if (fileName.equals("config")) {
                    mainConfigurations.put(versionExample, versionConfig);
                }
                processInheritance(versionExample, versionConfig, fileName, getMainConfiguration(versionExample));
                ConfigurationUtils.addConfigurations(config, versionConfig, true, true);
                getLogger().info(" Using compatibility configs: " + versionFileName);
            } catch (Exception ex) {
                getLogger().severe("Error loading file: " + versionFileName);
                throw ex;
            }
        } else {
            ConfigurationSection versionConfig = ConfigurationUtils.newConfigurationSection();
            processInheritance(versionExample, versionConfig, fileName, getMainConfiguration(versionExample));
            if (!versionConfig.getKeys(false).isEmpty()) {
                ConfigurationUtils.addConfigurations(config, versionConfig, true, true);
                getLogger().info(" Using inherited compatibility configs: " + versionFileName);
            }
        }
    }

    private void deleteDefaults(String defaultsFileName) {
        File savedDefaults = new File(configFolder, defaultsFileName);
        if (savedDefaults.exists()) {
            try {
                savedDefaults.delete();
                getLogger().info("Deleting defaults file: " + defaultsFileName + ", save_default_configs is false");
            } catch (Exception ex) {
                getLogger().warning("Couldn't delete defaults file: " + defaultsFileName + ", contents may be outdated");
            }
        }
    }

    private void disableAll(ConfigurationSection config) {
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            ConfigurationSection thisConfig = config.getConfigurationSection(key);
            if (thisConfig == null) continue;
            thisConfig.set("enabled", false);
        }
    }

    private void reenableAll(ConfigurationSection config, ConfigurationSection from) {
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            ConfigurationSection thisConfig = config.getConfigurationSection(key);
            if (thisConfig == null) continue;
            ConfigurationSection fromConfig = from.getConfigurationSection(key);
            if (fromConfig != null && fromConfig.getBoolean("enabled", true)) {
                thisConfig.set("enabled", true);
            }
        }
    }

    private void enableAll(ConfigurationSection config) {
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            ConfigurationSection thisConfig = config.getConfigurationSection(key);
            if (thisConfig == null || thisConfig.contains("enabled")) continue;
            thisConfig.set("enabled", true);
        }
    }

    private void clearEnabled(ConfigurationSection config) {
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            ConfigurationSection thisConfig = config.getConfigurationSection(key);
            if (thisConfig == null || !thisConfig.getBoolean("enabled")) continue;
            thisConfig.set("enabled", null);
        }
    }

    private ConfigurationSection loadConfigFolder(ConfigurationSection config, File configSubFolder, boolean reenable)
        throws IOException, InvalidConfigurationException {
        if (configSubFolder.exists()) {
            List<File> priorityFiles = new ArrayList<>();
            File[] files = configSubFolder.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(".")) continue;
                if (file.isDirectory()) {
                    config = loadConfigFolder(config, file, reenable);
                } else {
                    if (!file.getName().endsWith(".yml")) continue;
                    if (file.getName().startsWith("_")) {
                        priorityFiles.add(file);
                        continue;
                    }
                    try {
                        ConfigurationSection fileOverrides = CompatibilityUtils.loadConfiguration(file);
                        info(" Loading " + file.getName());
                        if (reenable) {
                            enableAll(fileOverrides);
                        }
                        config = ConfigurationUtils.addConfigurations(config, fileOverrides);
                    } catch (Exception ex) {
                        getLogger().severe("Error loading: " + file.getName());
                        throw ex;
                    }
                }
            }
            for (File file : priorityFiles) {
                try {
                    ConfigurationSection fileOverrides = CompatibilityUtils.loadConfiguration(file);
                    info(" Loading " + file.getName());
                    if (reenable) {
                        enableAll(fileOverrides);
                    }
                    config = ConfigurationUtils.addConfigurations(config, fileOverrides);
                } catch (Exception ex) {
                    getLogger().severe("Error loading: " + file.getName());
                    throw ex;
                }
            }
        } else {
            configSubFolder.mkdir();
        }

        return config;
    }

    private ConfigurationSection mapSpells(ConfigurationSection spellConfiguration) throws InvalidConfigurationException, IOException {
        ConfigurationSection spellConfigs = ConfigurationUtils.newConfigurationSection();
        if (spellConfiguration == null) return spellConfigs;

        // Reset cached spell configs
        spellConfigurations.clear();
        baseSpellConfigurations.clear();

        Set<String> spellKeys = spellConfiguration.getKeys(false);
        for (String key : spellKeys) {
            if (key.equals("default") || key.equals("override")) continue;

            ConfigurationSection spellNode = getSpellConfig(key, spellConfiguration);
            if (!ConfigurationUtils.isEnabled(spellNode)) {
                continue;
            }

            // Kind of a hacky way to do this, and only works with BaseSpell spells.
            if (noPvpRestricted) {
                spellNode.set("pvp_restricted", false);
            } else if (allPvpRestricted) {
                spellNode.set("pvp_restricted", true);
            }

            spellConfigs.set(key, spellNode);
        }

        return spellConfigs;
    }

    @Nullable
    private ConfigurationSection getSpellConfig(String key, ConfigurationSection config) {
        return getSpellConfig(key, config, true);
    }

    @Nullable
    private ConfigurationSection getSpellConfig(String key, ConfigurationSection config, boolean addInherited) {
        resolvingKeys.clear();
        return getSpellConfig(key, config, addInherited, resolvingKeys);
    }

    @Nullable
    private ConfigurationSection getSpellConfig(String key, ConfigurationSection config, boolean addInherited, Set<String> resolving) {
        // Catch circular dependencies
        if (resolvingKeys.contains(key)) {
            getLogger().log(Level.WARNING, "Circular dependency detected in spell configs: " + StringUtils.join(resolvingKeys, " -> ") + " -> " + key);
            return config;
        }
        resolvingKeys.add(key);

        if (addInherited) {
            ConfigurationSection built = spellConfigurations.get(key);
            if (built != null) {
                return built;
            }
        } else {
            ConfigurationSection built = baseSpellConfigurations.get(key);
            if (built != null) {
                return built;
            }
        }
        ConfigurationSection spellNode = config.getConfigurationSection(key);
        if (spellNode == null)
        {
            getLogger().warning("Spell " + key + " not known");
            return null;
        }
        spellNode = ConfigurationUtils.cloneConfiguration(spellNode);

        SpellKey spellKey = new SpellKey(key);
        String inheritFrom = spellNode.getString("inherit");
        if (inheritFrom != null && inheritFrom.equalsIgnoreCase("false"))
        {
            inheritFrom = null;
        }
        String upgradeInheritsFrom = null;
        if (spellKey.isVariant()) {
            if (!spellUpgradesEnabled) {
                return null;
            }
            int level = spellKey.getLevel();
            upgradeInheritsFrom = spellKey.getBaseKey();
            if (level != 2) {
                upgradeInheritsFrom += "|" + (level - 1);
            }
        }

        boolean processInherited = addInherited && inheritFrom != null;
        if (processInherited || upgradeInheritsFrom != null)
        {
            if (processInherited && key.equals(inheritFrom))
            {
                getLogger().warning("Spell " + key + " inherits from itself");
            }
            else if (processInherited)
            {
                ConfigurationSection inheritConfig = getSpellConfig(inheritFrom, config, true, resolving);
                if (inheritConfig != null)
                {
                    Boolean enabled = spellNode.contains("enabled") ? spellNode.getBoolean("enabled") : null;
                    spellNode = ConfigurationUtils.addConfigurations(spellNode, inheritConfig, false);
                    spellNode.set("enabled", enabled);
                }
                else
                {
                    getLogger().warning("Spell " + key + " inherits from unknown ancestor " + inheritFrom);
                }
            }

            if (upgradeInheritsFrom != null)
            {
                if (config.contains(upgradeInheritsFrom))
                {
                    ConfigurationSection baseInheritConfig = getSpellConfig(upgradeInheritsFrom, config, inheritFrom == null, resolving);
                    spellNode = ConfigurationUtils.addConfigurations(spellNode, baseInheritConfig, inheritFrom != null);
                } else {
                    getLogger().warning("Spell upgrade " + key + " inherits from unknown level " + upgradeInheritsFrom);
                }
            }
        } else {
            ConfigurationSection defaults = config.getConfigurationSection("default");
            if (defaults != null) {
                spellNode = ConfigurationUtils.addConfigurations(spellNode, defaults, false);
            }
        }

        if (addInherited) {
            spellConfigurations.put(key, spellNode);
        } else {
            baseSpellConfigurations.put(key, spellNode);
        }

        // Apply spell override last
        ConfigurationSection override = config.getConfigurationSection("override");
        if (override != null) {
            spellNode = ConfigurationUtils.addConfigurations(spellNode, override, true);
        }

        return spellNode;
    }

    private void run(boolean synchronous) {
        success = true;
        Logger logger = controller.getLogger();

        // Load main configuration
        try {
            mainConfiguration = loadMainConfiguration();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading config.yml", ex);
            success = false;
            mainConfiguration = ConfigurationUtils.newConfigurationSection();
        }
        mainConfigurations.put("", mainConfiguration);

        // Load other configurations
        loadedConfigurations.clear();
        for (String configurationFile : CONFIG_FILES) {
            try {
                ConfigurationSection configuration = loadConfigFile(configurationFile, mainConfiguration);

                // Spells require special processing
                if (configurationFile.equals("spells")) {
                    configuration = mapSpells(configuration);
                }

                loadedConfigurations.put(configurationFile, configuration);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error loading " + configurationFile, ex);
                loadedConfigurations.put(configurationFile, ConfigurationUtils.newConfigurationSection());
                success = false;
            }
        }

        // Finalize configuration load
        if (synchronous) {
            controller.finalizeLoad(this, sender);
        } else {
            Plugin plugin = controller.getPlugin();
            final ConfigurationLoadTask result = this;
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    controller.finalizeLoad(result, sender);
                }
            });
        }
    }

    @Override
    public void run() {
        synchronized (loadLock) {
            run(false);
        }
    }

    public void runNow() {
        synchronized (loadLock) {
            run(true);
        }
    }

    public ConfigurationSection getMainConfiguration() {
        return mainConfiguration;
    }

    @Nonnull
    public ConfigurationSection getMainConfiguration(String exampleKey) {
        ConfigurationSection mainConfig = mainConfigurations.get(exampleKey);
        if (mainConfig == null) {
            mainConfig = ConfigurationUtils.newConfigurationSection();
            mainConfigurations.put(exampleKey, mainConfig);
        }
        return mainConfig;
    }

    public ConfigurationSection getMessages() {
        return loadedConfigurations.get("messages");
    }

    public ConfigurationSection getMaterials() {
        return loadedConfigurations.get("materials");
    }

    public ConfigurationSection getWands() {
        return loadedConfigurations.get("wands");
    }

    public ConfigurationSection getPaths() {
        return loadedConfigurations.get("paths");
    }

    public ConfigurationSection getCrafting() {
        return loadedConfigurations.get("crafting");
    }

    public ConfigurationSection getMobs() {
        return loadedConfigurations.get("mobs");
    }

    public ConfigurationSection getItems() {
        return loadedConfigurations.get("items");
    }

    public ConfigurationSection getClasses() {
        return loadedConfigurations.get("classes");
    }

    public ConfigurationSection getModifiers() {
        return loadedConfigurations.get("modifiers");
    }

    public ConfigurationSection getAttributes() {
        return loadedConfigurations.get("attributes");
    }

    public ConfigurationSection getAutomata() {
        return loadedConfigurations.get("automata");
    }

    public ConfigurationSection getEffects() {
        return loadedConfigurations.get("effects");
    }

    public ConfigurationSection getSpells() {
        return loadedConfigurations.get("spells");
    }

    public ConfigurationSection getWorlds() {
        return loadedConfigurations.get("worlds");
    }

    public ConfigurationSection getKits() {
        return loadedConfigurations.get("kits");
    }

    public boolean isSuccessful() {
        return success;
    }

    public String getExampleDefaults() {
        return exampleDefaults;
    }

    public Collection<String> getAddExamples() {
        return addExamples;
    }

    public Map<String, String> getExampleKeyNames() {
        return exampleKeyNames;
    }
}
