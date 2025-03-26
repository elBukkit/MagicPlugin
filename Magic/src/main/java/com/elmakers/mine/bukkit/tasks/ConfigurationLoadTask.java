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
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.configuration.MagicConfiguration;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.google.common.collect.ImmutableSet;

public class ConfigurationLoadTask implements Runnable {
    private final MagicController controller;
    private final File configFolder;
    private final Plugin plugin;
    private final CommandSender sender;
    private boolean verboseLogging;

    private static final String[] CONFIG_FILES = {"messages", "materials", "attributes", "effects", "spells", "paths",
            "classes", "wands", "items", "kits", "crafting", "mobs", "blocks", "modifiers", "worlds", "arenas", "icons"};

    private static final ImmutableSet<String> DEFAULT_ON = ImmutableSet.of("messages", "materials");

    private final Map<String, ConfigurationSection> loadedConfigurations = new HashMap<>();
    private final Map<String, ConfigurationSection> mainConfigurations = new HashMap<>();

    private final Map<String, ConfigurationSection> spellConfigurations     = new HashMap<>();
    private final Map<String, ConfigurationSection> baseSpellConfigurations = new HashMap<>();
    private final Map<String, ConfigurationSection> exampleConfigurations = new HashMap<>();
    private final Map<String, String> exampleKeyNames = new HashMap<>();

    private final Map<String, ConfigurationSection> builtinConfigurations = new HashMap<>();
    private final Map<String, ConfigurationSection> loadedConfigurationFiles = new HashMap<>();

    private final Map<String, ConfigurationSection> addDisabled = new HashMap<>();

    private static final Object loadLock = new Object();

    private boolean allPvpRestricted = false;
    private boolean noPvpRestricted = false;
    private boolean saveDefaultConfigs = true;
    private boolean spellUpgradesEnabled = true;

    private String exampleDefaults = null;
    private String languageOverride = null;
    private List<String> addExamples = null;
    private Set<String> allExamples = new HashSet<>();

    private final ConfigurationSection helpTopics;
    private ConfigurationSection mainConfiguration;

    private Set<String> resolvingKeys = new LinkedHashSet<>();

    private boolean success;

    public ConfigurationLoadTask(MagicController controller, CommandSender sender) {
        this.controller = controller;
        this.sender = sender;
        plugin = controller.getPlugin();
        configFolder = controller.getConfigFolder();
        helpTopics = ConfigurationUtils.newConfigurationSection();
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
        return loadExampleConfiguration(examplesPrefix, exampleKey, true);
    }

    @Nonnull
    private ConfigurationSection loadExampleConfiguration(String examplesPrefix, String exampleKey, boolean processMessages) {
        ConfigurationSection exampleConfig = exampleConfigurations.get(examplesPrefix);
        if (exampleConfig == null) {
            boolean isMainConfig = examplesPrefix.endsWith("config");
            boolean isMessagesConfig = examplesPrefix.endsWith("messages");
            String examplesFileName = examplesPrefix + ".yml";
            File externalFolder = new File(plugin.getDataFolder(), examplesPrefix);
            File externalFile = new File(plugin.getDataFolder(), examplesFileName);
            if (externalFile.exists()) {
                try {
                    exampleConfig = loadConfiguration(examplesPrefix, externalFile);
                } catch (Exception ex) {
                    getLogger().severe("Error loading: " + examplesFileName);
                }
            }
            if (externalFolder.exists()) {
                try {
                    if (exampleConfig == null) {
                        exampleConfig = ConfigurationUtils.newConfigurationSection();
                    }
                    exampleConfig = loadConfigFolder(examplesPrefix, exampleConfig, externalFolder);
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
                        exampleConfig = CompatibilityLib.getCompatibilityUtils().loadConfiguration(input, examplesFileName);
                    } catch (Exception ex) {
                        getLogger().log(Level.SEVERE, "Error loading: " + examplesFileName + " from builtin resources", ex);
                    }
                }
            }
            if (exampleConfig == null) {
                exampleConfig = ConfigurationUtils.newConfigurationSection();
            } else if (isMessagesConfig && processMessages) {
                processMessageExample(exampleConfig, exampleConfig.getBoolean("example_override", false));
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
            config = CompatibilityLib.getCompatibilityUtils().loadBuiltinConfiguration(defaultsFileName);
        } catch (Exception ex) {
            getLogger().severe("Error loading file: " + defaultsFileName);
            throw ex;
        }
        info(" Based on defaults " + defaultsFileName);

        // Load lists
        String listsFilename = "defaults/lists.defaults.yml";
        YamlConfiguration listConfig = null;
        try {
            listConfig = CompatibilityLib.getCompatibilityUtils().loadBuiltinConfiguration(listsFilename);
            ConfigurationUtils.addConfigurations(config, listConfig);
        } catch (Exception ex) {
            getLogger().severe("Error loading file: " + listsFilename);
            throw ex;
        }
        info(" Added lists from " + listsFilename);

        // Load an example if one is specified
        if (usingExample) {
            ConfigurationSection exampleConfig = loadExampleConfiguration(examplesFilePrefix, exampleDefaults);

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
        if (addExamples != null && addExamples.size() > 0) {
            for (String example : addExamples) {
                examplesFilePrefix = "examples/" + example + "/" + fileName;

                ConfigurationSection exampleConfig = loadExampleConfiguration(examplesFilePrefix, example);
                try {
                    boolean override = exampleConfig.getBoolean("example_override", false);
                    exampleConfig.set("example_override", null);
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

        // Look for any missing required configurations
        List<String> requirements = new ArrayList<>();
        for (Map.Entry<String, ConfigurationSection> entry : mainConfigurations.entrySet()) {
            List<String> requires = ConfigurationUtils.getStringList(entry.getValue(), "require");
            if (requires == null) continue;
            String exampleKey = entry.getKey();
            for (String require : requires) {
                // If we have requirements, we can't really be the main config
                // This will not handle ordering multiple chains of requirements, so.. don't do that
                boolean isMainConfig = exampleDefaults != null && exampleDefaults.equals(exampleKey);
                if (isMainConfig) {
                    info("Switching main example from " + exampleKey + " to required example " + require + "");
                    exampleDefaults = require;
                    addExamples.remove(require);
                    addExamples.add(exampleKey);

                }
                if (!mainConfigurations.containsKey(require)) {
                    requirements.add(require);
                    if (!isMainConfig) {
                        addExamples.add(0, require);
                    }
                    info("Force-loading " + require + " because it is required by " + exampleKey);
                }
            }
        }

        for (String example : requirements) {
            examplesFilePrefix = "examples/" + example + "/" + fileName;

            ConfigurationSection exampleConfig = loadExampleConfiguration(examplesFilePrefix, example);
            try {
                boolean override = exampleConfig.getBoolean("example_override", false);
                exampleConfig.set("example_override", null);
                info(" Adding " + examplesFilePrefix + (override ? ", allowing overrides" : ""));
                processInheritance(example, exampleConfig, fileName, exampleConfig);
                mainConfigurations.put(example, exampleConfig);
                ConfigurationUtils.addConfigurations(config, exampleConfig, override);
            } catch (Exception ex) {
                getLogger().severe("Error loading: " + examplesFilePrefix);
                throw ex;
            }
        }

        // Apply version-specific configs
        addVersionConfigs(config, fileName);

        // Apply overrides after loading defaults and examples
        ConfigurationUtils.addConfigurations(config, overrides);

        // Apply file overrides last
        File configSubFolder = new File(configFolder, fileName);
        loadConfigFolder(fileName, config, configSubFolder);

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
        boolean isMainConfig = fileName.equals("config");
        boolean isUnkeyedConfig = isMainConfig || fileName.equals("messages") || fileName.equals("materials");
        if (inherited == null) {
            inherited = new LinkedHashSet<>();
        } else {
            inherited = new LinkedHashSet<>(inherited);
        }
        inherited.add(exampleKey);
        List<String> inherits = ConfigurationUtils.getStringList(mainConfiguration, "inherit");
        if (inherits != null) {
            List<String> skipList = ConfigurationUtils.getStringList(mainConfiguration, "skip_inherited");
            List<String> includeList = ConfigurationUtils.getStringList(mainConfiguration, "include_inherited");
            boolean skip = skipList != null && skipList.contains(fileName);
            boolean include = includeList == null || includeList.contains(fileName);
            if (!skip && include) {
                for (String inheritFrom : inherits) {
                    String inheritFilePrefix = "examples/" + inheritFrom + "/" + fileName;

                    ConfigurationSection inheritedConfig = loadExampleConfiguration(inheritFilePrefix, inheritFrom);
                    if (isMainConfig) {
                        mainConfigurations.put(inheritFrom, ConfigurationUtils.cloneConfiguration(inheritedConfig));

                        // These should not be inherited
                        inheritedConfig.set("disable_inherited", null);
                        inheritedConfig.set("skip_inherited", null);
                        inheritedConfig.set("include_inherited", null);
                        inheritedConfig.set("example", null);
                        inheritedConfig.set("inherit", null);
                    }
                    try {
                        if (inherited.contains(inheritFrom)) {
                            getLogger().log(Level.WARNING, "    Circular dependency detected in configuration inheritance: " + StringUtils.join(inherited, " -> ") + " -> " + inheritFrom);
                        } else {
                            processInheritance(inheritFrom, inheritedConfig, fileName, getMainConfiguration(inheritFrom), inherited);
                        }
                        List<String> disable = ConfigurationUtils.getStringList(mainConfiguration, "disable_inherited");
                        if (!isUnkeyedConfig && disable != null && disable.contains(fileName)) {
                            addDisabled(inheritFrom, inheritedConfig);
                            info("   Example " + exampleKey + " inheriting from disabled " + inheritFrom);
                        } else {
                            ConfigurationUtils.addConfigurations(exampleConfig, inheritedConfig, false);
                            info("   Example " + exampleKey + " inheriting from " + inheritFrom);
                        }
                    } catch (Exception ex) {
                        getLogger().severe("Error loading file: " + inheritFilePrefix);
                        throw ex;
                    }
                }
            }
        }
    }

    @Nonnull
    private ConfigurationSection getBuiltin(String fileType) {
        ConfigurationSection builtin = builtinConfigurations.get(fileType);
        if (builtin == null) {
            File targetFile = new File(controller.getPlugin().getDataFolder(), "defaults/" + fileType + ".defaults.yml");
            if (!targetFile.exists()) {
                controller.getLogger().warning("Missing builtin default file for " + fileType);
                return ConfigurationUtils.newConfigurationSection();
            }
            try {
                YamlConfiguration newFile = new YamlConfiguration();
                newFile.load(targetFile);
                builtin = newFile;
                builtinConfigurations.put(fileType, builtin);
            } catch (Exception ex) {
                controller.getLogger().log(Level.WARNING, "Error loading defaults file: " + targetFile.getAbsolutePath(), ex);
                return ConfigurationUtils.newConfigurationSection();
            }
        }
        return builtin;
    }

    private void checkBuiltin(String fileType, File file, ConfigurationSection config) {
        if (!(config instanceof YamlConfiguration)) {
            return;
        }

        YamlConfiguration yaml = (YamlConfiguration)config;
        String header = yaml.options().header();
        // Yeah really ugly, ik, ik
        if (header == null || !header.contains("file is merged from the files")) {
            return;
        }

        controller.getLogger().info("Note: You have a " + fileType + " at " + file.getAbsolutePath() + " that was copied from the defaults, will ignore anything that is set to the same as defaults to avoid unintentionally overriding loaded examples");
        ConfigurationSection builtinConfig = getBuiltin(fileType);
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            Object configValue = config.get(key);
            Object builtinValue = builtinConfig.get(key);
            if (Objects.equals(configValue, builtinValue)) {
                config.set(key, null);
            }
        }
    }

    private ConfigurationSection loadConfiguration(String fileType, File configFile) throws IOException, InvalidConfigurationException {
        String path = configFile.getAbsolutePath();
        ConfigurationSection config = loadedConfigurationFiles.get(path);
        if (config == null) {
            config = CompatibilityLib.getCompatibilityUtils().loadConfiguration(configFile);
            checkBuiltin(fileType, configFile, config);
            loadedConfigurationFiles.put(path, config);
        }
        return config;
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
            results = loadConfiguration(fileName, configFile);
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
        boolean isUnkeyedConfig = fileName.equals("config") || fileName.equals("messages") || fileName.equals("materials");
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
            defaultConfig = CompatibilityLib.getCompatibilityUtils().loadBuiltinConfiguration(defaultsFileName);
        } catch (Exception ex) {
            getLogger().severe("Error loading file: " + defaultsFileName);
            throw ex;
        }
        String header = defaultConfig.options().header();

        // Load defaults, I kind of think we should always do this but I'm leaving this if here
        // for backwards-compatibility
        // I did remove the ability to disable defaults but there's really nothing in the defaults
        // anymore so I don't think this should be a problem.
        if (loadDefaults) {
            info(" Based on defaults " + defaultsFileName);
            ConfigurationUtils.addConfigurations(config, defaultConfig);
        }

        // Load example
        if (usingExample && loadDefaults) {
            ConfigurationSection exampleConfig = loadExampleConfiguration(examplesFilePrefix, exampleDefaults);
            try {
                processInheritance(exampleDefaults, exampleConfig, fileName, getMainConfiguration(exampleDefaults));
                if (disableDefaults) {
                    addDisabled(exampleDefaults, exampleConfig);
                    info(" Using disabled " + examplesFilePrefix);
                } else {
                    ConfigurationUtils.addConfigurations(config, exampleConfig);
                    info(" Using " + examplesFilePrefix);
                }
            } catch (Exception ex) {
                getLogger().severe("Error loading file: " + examplesFilePrefix);
                throw ex;
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
                try {
                    processInheritance(example, exampleConfig, fileName, getMainConfiguration(example));
                    // Don't override messages or config when adding an example, but allow other config overrides
                    // Unless otherwise specified
                    boolean override = exampleConfig.getBoolean("example_override", false);
                    exampleConfig.set("example_override", null);
                    ConfigurationUtils.addConfigurations(config, exampleConfig, !isUnkeyedConfig || override);
                    info(" Added " + examplesFilePrefix + (override ? ", allowing overrides" : ""));
                } catch (Exception ex) {
                    getLogger().severe("Error loading file: " + examplesFilePrefix);
                    throw ex;
                }
            }
        }

        // Apply version-specific configs
        addVersionConfigs(config, fileName);

        // Special processing for the messages files
        boolean isMessages = fileName.equals("messages");

        // Process help topics if this is messages
        if (isMessages) {
            processHelpTopics(config);
        }

        // Apply language overrides, but only to the messages config
        // These will completely replace merged help topics, and so should include all examples if possible
        if (isMessages && languageOverride != null && !languageOverride.isEmpty() && !languageOverride.equalsIgnoreCase("EN")) {
            String languageFilePrefix = "examples/localizations/messages." + languageOverride;
            ConfigurationSection languageConfig = loadExampleConfiguration(languageFilePrefix, "localizations", false);
            try {
                ConfigurationUtils.addConfigurations(config, languageConfig);
                info(" Using " + languageFilePrefix);
            } catch (Exception ex) {
                getLogger().severe("Error loading file: " + languageFilePrefix);
                throw ex;
            }
        }

        // Apply overrides after loading defaults and examples
        ConfigurationUtils.addConfigurations(config, overrides, true, false, true);

        // Apply file overrides last
        File configSubFolder = new File(configFolder, fileName);
        loadConfigFolder(fileName, config, configSubFolder);

        // But actually really last, add in anything we need that was loaded as disabled
        resolveDisabled(config);

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

    private void processHelpTopics(ConfigurationSection messagesConfig) {
        ConfigurationSection mainHelp = messagesConfig.getConfigurationSection("help");
        // This shouldn't ever happen since there's a help section in defaults, but still.
        if (mainHelp == null) {
            return;
        }

        ConfigurationUtils.mergeText(mainHelp, helpTopics);
    }

    private void processMessageExample(ConfigurationSection example, boolean override) {
        // Hacky special handling for the help system
        // This lets examples add new content to existing help topics.
        ConfigurationSection exampleHelp = example.getConfigurationSection("help");
        if (exampleHelp == null) {
            return;
        }
        if (override) {
            ConfigurationUtils.addConfigurations(helpTopics, exampleHelp, true);
        } else {
            ConfigurationUtils.mergeText(helpTopics, exampleHelp);
        }

        // Need to make sure we don't end up overwriting the merged text with the example
        // loadExampleConfiguration returns a clone, so this should be OK to do.
        example.set("help", null);
    }

    private ConfigurationSection loadLegacyConfigFile(String fileName, String modernFilename, ConfigurationSection mainConfiguration) {
        boolean loadAllDefaults = mainConfiguration.getBoolean("load_default_configs", true);
        boolean loadDefaults = mainConfiguration.getBoolean("load_default_" + modernFilename, loadAllDefaults);
        boolean disableDefaults = mainConfiguration.getBoolean("disable_default_" + modernFilename, false);

        ConfigurationSection mainSection = mainConfiguration.getConfigurationSection(fileName);
        boolean usingExample = exampleDefaults != null && exampleDefaults.length() > 0;
        String examplesFilePrefix = usingExample ? "examples/" + exampleDefaults + "/" + fileName : null;

        YamlConfiguration config = new YamlConfiguration();

        // Load example
        if (usingExample && loadDefaults) {
            ConfigurationSection exampleConfig = loadExampleConfiguration(examplesFilePrefix, exampleDefaults);
            try {
                processInheritance(exampleDefaults, exampleConfig, fileName, getMainConfiguration(exampleDefaults));
                if (disableDefaults) {
                    addDisabled(exampleDefaults, exampleConfig);
                    info(" Using disabled " + examplesFilePrefix);
                } else {
                    ConfigurationUtils.addConfigurations(config, exampleConfig);
                    info(" Using " + examplesFilePrefix);
                }
            } catch (Exception ex) {
                getLogger().severe("Error loading file: " + examplesFilePrefix);
                throw ex;
            }
        }

        // Load anything relevant from the main config
        if (mainSection != null) {
            ConfigurationUtils.addConfigurations(config, mainSection);
        }

        // Add in examples
        if (addExamples != null && addExamples.size() > 0) {
            for (String example : addExamples) {
                examplesFilePrefix = "examples/" + example + "/" + fileName;
                ConfigurationSection exampleConfig = loadExampleConfiguration(examplesFilePrefix, example);
                try {
                    processInheritance(example, exampleConfig, fileName, getMainConfiguration(example));
                    ConfigurationUtils.addConfigurations(config, exampleConfig, false);
                    info(" Added " + examplesFilePrefix);
                } catch (Exception ex) {
                    getLogger().severe("Error loading file: " + examplesFilePrefix);
                    throw ex;
                }
            }
        }

        return config;
    }

    private void addVersionConfigs(ConfigurationSection config, String fileName) throws InvalidConfigurationException, IOException {
        int[] serverVersion = CompatibilityLib.getServerVersion(plugin);
        int majorVersion = serverVersion[0];
        int minorVersion = serverVersion[1];
        int extraMinorVersion = serverVersion[2];
        String versionExample = majorVersion + "." + minorVersion;
        String versionFileName = "examples/versions/" + versionExample + "/" + fileName + ".yml";
        InputStream versionInput = plugin.getResource(versionFileName);
        if (versionInput == null) {
            versionExample = majorVersion + "." + minorVersion + "." + extraMinorVersion;
            versionFileName = "examples/versions/" + versionExample + "/" + fileName + ".yml";
            versionInput = plugin.getResource(versionFileName);
        }
        if (versionInput != null)  {
            try {
                ConfigurationSection versionConfig = CompatibilityLib.getCompatibilityUtils().loadConfiguration(versionInput, versionFileName);
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
                getLogger().info(" Using inherited compatibility configs for: " + versionFileName);
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

    private void resolveDisabled(ConfigurationSection config) {
        for (ConfigurationSection disableConfigs : addDisabled.values()) {
            Set<String> keys = disableConfigs.getKeys(false);
            for (String key : keys) {
                ConfigurationSection disableConfig = disableConfigs.getConfigurationSection(key);
                if (disableConfig == null) continue;

                // If something else has already included this config, use that instead.
                ConfigurationSection existing = config.getConfigurationSection(key);
                if (existing == null) {
                    // Include this config, but make it disabled
                    disableConfig.set("enabled", false);
                    config.set(key, disableConfig);
                } else {
                    existing = ConfigurationUtils.addConfigurations(existing, disableConfig, false);
                    config.set(key, existing);
                }
            }
        }
        addDisabled.clear();
    }

    private void addDisabled(String exampleKey, ConfigurationSection configuration) {
        addDisabled.put(exampleKey, configuration);
    }

    private ConfigurationSection loadConfigFolder(String fileType, ConfigurationSection config, File configSubFolder)
        throws IOException, InvalidConfigurationException {
        if (configSubFolder.exists()) {
            List<File> priorityFiles = new ArrayList<>();
            File[] files = configSubFolder.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(".")) continue;
                if (file.isDirectory()) {
                    config = loadConfigFolder(fileType, config, file);
                } else {
                    if (!file.getName().endsWith(".yml")) continue;
                    if (file.getName().startsWith("_")) {
                        priorityFiles.add(file);
                        continue;
                    }
                    try {
                        ConfigurationSection fileOverrides = loadConfiguration(fileType, file);
                        info(" Loading " + file.getName());
                        config = ConfigurationUtils.addConfigurations(config, fileOverrides, true, false, true);
                    } catch (Exception ex) {
                        getLogger().severe("Error loading: " + file.getName());
                        throw ex;
                    }
                }
            }
            for (File file : priorityFiles) {
                try {
                    ConfigurationSection fileOverrides = CompatibilityLib.getCompatibilityUtils().loadConfiguration(file);
                    info(" Loading " + file.getName());
                    config = ConfigurationUtils.addConfigurations(config, fileOverrides, true, false, true);
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

    private ConfigurationSection mapSpells(ConfigurationSection spellConfiguration) {
        ConfigurationSection spellConfigs = ConfigurationUtils.newConfigurationSection();
        if (spellConfiguration == null) return spellConfigs;

        // Reset cached spell configs
        spellConfigurations.clear();
        baseSpellConfigurations.clear();

        Set<String> spellKeys = spellConfiguration.getKeys(false);
        for (String key : spellKeys) {
            if (key.equals("default") || key.equals("override")) continue;

            ConfigurationSection spellNode = getSpellConfig(key, spellConfiguration);
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
        if (resolving.contains(key)) {
            getLogger().log(Level.WARNING, "Circular dependency detected in spell configs: " + StringUtils.join(resolving, " -> ") + " -> " + key);
            return config;
        }
        resolving.add(key);

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
            getLogger().warning("Could not resolve spell " + key + " from inheritance path: " + StringUtils.join(resolving, " -> "));
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
            mainConfiguration = new MagicConfiguration(controller, loadMainConfiguration(), "config");
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

                // Blocks may have legacy configs spread across examples
                // there should not be any automata configs in the root Magic folder though
                if (configurationFile.equals("blocks")) {
                    ConfigurationSection legacyConfig = loadLegacyConfigFile("automata", "blocks", mainConfiguration);
                    configuration = ConfigurationUtils.addConfigurations(configuration, legacyConfig, false);
                }

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

    public ConfigurationSection getBlocks() {
        ConfigurationSection legacyConfig = loadedConfigurations.get("automata");
        ConfigurationSection newConfig = loadedConfigurations.get("blocks");
        return ConfigurationUtils.addConfigurations(newConfig, legacyConfig, false);
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

    public ConfigurationSection getArenas() {
        return loadedConfigurations.get("arenas");
    }

    public ConfigurationSection getIcons() {
        return loadedConfigurations.get("icons");
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
