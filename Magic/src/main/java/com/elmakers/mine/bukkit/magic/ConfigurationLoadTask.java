package com.elmakers.mine.bukkit.magic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ConfigurationLoadTask implements Runnable {
    private final MagicController controller;
    private final File configFolder;
    private final Plugin plugin;
    private final CommandSender sender;

    private static final String[] CONFIG_FILES = {"messages", "materials", "attributes", "effects", "spells", "paths",
            "classes", "wands", "items", "crafting", "mobs", "automata"};

    private final Map<String, ConfigurationSection> loadedConfigurations = new HashMap<>();

    private final Map<String, ConfigurationSection> spellConfigurations     = new HashMap<>();
    private final Map<String, ConfigurationSection> baseSpellConfigurations = new HashMap<>();

    private static final Object loadLock = new Object();

    private boolean allPvpRestricted = false;
    private boolean noPvpRestricted = false;
    private boolean saveDefaultConfigs = true;
    private boolean spellUpgradesEnabled = true;

    private String exampleDefaults = null;
    private Collection<String> addExamples = null;
    private Collection<String> githubExamples = null;

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
        addExamples = properties.getStringList("add_examples");
        githubExamples = properties.getStringList("github_configs");
    }

    private ConfigurationSection loadMainConfiguration() throws InvalidConfigurationException, IOException {
        ConfigurationSection configuration = loadMainConfiguration("config");
        loadInitialProperties(configuration);

        if (githubExamples != null && githubExamples.size() > 0) {
            getLogger().info("Loading remote configurations: " + githubExamples);

            // Reload config, examples will be used this time.
            configuration = loadMainConfiguration("config");

            // Reload initial properties, in case the remote configs specify examples
            loadInitialProperties(configuration);
        }
        if (addExamples != null && addExamples.size() > 0) {
            getLogger().info("Adding examples: " + StringUtils.join(addExamples, ","));
        }
        if (exampleDefaults != null && exampleDefaults.length() > 0) {
            getLogger().info("Overriding configuration with example: " + exampleDefaults);

            // Reload config, examples will be used this time.
            configuration = loadMainConfiguration("config");
        }

        return configuration;
    }

    private ConfigurationSection loadMainConfiguration(String fileName) throws InvalidConfigurationException, IOException {
        ConfigurationSection overrides = loadOverrides(fileName);

        boolean usingExample = exampleDefaults != null && exampleDefaults.length() > 0;
        String examplesFileName = usingExample ? "examples/" + exampleDefaults + "/" + fileName + ".yml" : null;
        String defaultsFileName = "defaults/" + fileName + ".defaults.yml";

        // Start with default configs
        YamlConfiguration config = CompatibilityUtils.loadConfiguration(plugin.getResource(defaultsFileName));
        getLogger().info(" Based on defaults " + defaultsFileName);

        // Load an example if one is specified
        if (usingExample) {
            InputStream input = plugin.getResource(examplesFileName);
            if (input != null)  {
                ConfigurationSection exampleConfig = CompatibilityUtils.loadConfiguration(input);
                ConfigurationUtils.addConfigurations(config, exampleConfig);
                getLogger().info(" Using " + examplesFileName);
            }
        }

        // Load remote examples if specified
        if (githubExamples != null) {
            for (String url : githubExamples) {
                ConfigurationSection remoteConfig = loadGithubConfiguration(fileName, url);
                ConfigurationUtils.addConfigurations(config, remoteConfig);
            }
        }

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
            } catch (Exception ex) {
                getLogger().warning("Couldn't write defaults file: " + defaultsFileName);
            }
        } else  {
            deleteDefaults(defaultsFileName);
        }

        return config;
    }

    private ConfigurationSection loadOverrides(String fileName) throws IOException, InvalidConfigurationException {
        String configFileName = fileName + ".yml";
        File configFile = new File(configFolder, configFileName);
        if (!configFile.exists()) {
            getLogger().info("Saving template " + configFileName + ", edit to customize configuration.");
            plugin.saveResource(configFileName, false);
        }

        getLogger().info("Loading " + configFile.getName());
        return CompatibilityUtils.loadConfiguration(configFile);
    }

    private ConfigurationSection loadConfigFile(String fileName, ConfigurationSection mainConfiguration)
        throws IOException, InvalidConfigurationException {

        boolean loadDefaults = mainConfiguration.getBoolean("load_default_" + fileName, true);
        loadDefaults = loadDefaults && mainConfiguration.getBoolean("load_default_configs", true);
        boolean disableDefaults = mainConfiguration.getBoolean("disable_default_" + fileName, false);

        ConfigurationSection mainSection = mainConfiguration.getConfigurationSection(fileName);

        ConfigurationSection overrides = loadOverrides(fileName);
        boolean usingExample = exampleDefaults != null && exampleDefaults.length() > 0;

        String examplesFileName = usingExample ? "examples/" + exampleDefaults + "/" + fileName + ".yml" : null;
        String defaultsFileName = "defaults/" + fileName + ".defaults.yml";

        YamlConfiguration config = new YamlConfiguration();

        YamlConfiguration defaultConfig = CompatibilityUtils.loadConfiguration(plugin.getResource(defaultsFileName));
        String header = defaultConfig.options().header();

        // Load defaults
        if (loadDefaults) {
            getLogger().info(" Based on defaults " + defaultsFileName);
            if (disableDefaults) {
                disableAll(defaultConfig);
            }
            ConfigurationUtils.addConfigurations(config, defaultConfig);
        }

        // Load example
        if (usingExample && loadDefaults) {
            boolean disableInherited = false;
            // Load inherited configs first
            List<String> inherits = ConfigurationUtils.getStringList(mainConfiguration, "inherit");
            if (inherits != null) {
                List<String> skip = ConfigurationUtils.getStringList(mainConfiguration, "skip_inherited");
                if (!skip.contains(fileName)) {
                    for (String inheritFrom : inherits) {
                        String inheritFileName = "examples/" + inheritFrom + "/" + fileName + ".yml";
                        InputStream input = plugin.getResource(inheritFileName);
                        if (input != null) {
                            List<String> disable = ConfigurationUtils.getStringList(mainConfiguration, "disable_inherited");
                            ConfigurationSection inheritedConfig = CompatibilityUtils.loadConfiguration(input);

                            if (disable.contains(fileName)) {
                                disableInherited = true;
                                disableAll(inheritedConfig);
                            }

                            ConfigurationUtils.addConfigurations(config, inheritedConfig);
                            getLogger().info(" Inheriting from " + inheritFrom);
                        }
                    }
                }
            }

            InputStream input = plugin.getResource(examplesFileName);
            if (input != null) {
                ConfigurationSection exampleConfig = CompatibilityUtils.loadConfiguration(input);
                if (disableDefaults) {
                    disableAll(exampleConfig);
                } else if (disableInherited) {
                    enableAll(exampleConfig);
                }
                ConfigurationUtils.addConfigurations(config, exampleConfig);
                getLogger().info(" Using " + examplesFileName);
            }
        }

        // Load remote examples if specified
        if (githubExamples != null) {
            for (String url : githubExamples) {
                ConfigurationSection remoteConfig = loadGithubConfiguration(fileName, url);
                ConfigurationUtils.addConfigurations(config, remoteConfig);
            }
        }

        // Load anything relevant from the main config
        if (mainSection != null) {
            ConfigurationUtils.addConfigurations(overrides, mainSection);
        }

        // Re-enable anything we are overriding
        if (disableDefaults) {
            enableAll(overrides);
        }

        // Add in examples
        if (addExamples != null && addExamples.size() > 0) {
            for (String example : addExamples) {
                examplesFileName = "examples/" + example + "/" + fileName + ".yml";
                InputStream input = plugin.getResource(examplesFileName);
                if (input != null)
                {
                    ConfigurationSection exampleConfig = CompatibilityUtils.loadConfiguration(input);
                    if (disableDefaults) {
                        enableAll(exampleConfig);
                    }
                    ConfigurationUtils.addConfigurations(config, exampleConfig, false);
                    getLogger().info(" Added " + examplesFileName);
                }
            }
        }

        // Apply overrides after loading defaults and examples
        ConfigurationUtils.addConfigurations(config, overrides);

        // Apply file overrides last
        File configSubFolder = new File(configFolder, fileName);
        loadConfigFolder(config, configSubFolder, disableDefaults);

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

    private void enableAll(ConfigurationSection rootSection, boolean enabled) {
        Set<String> keys = rootSection.getKeys(false);
        for (String key : keys)
        {
            ConfigurationSection section = rootSection.getConfigurationSection(key);
            if (!section.isSet("enabled")) {
                section.set("enabled", enabled);
            }
        }
    }

    private void enableAll(ConfigurationSection rootSection) {
        enableAll(rootSection, true);
    }

    private void disableAll(ConfigurationSection rootSection) {
        enableAll(rootSection, false);
    }

    private ConfigurationSection loadConfigFolder(ConfigurationSection config, File configSubFolder, boolean setEnabled)
        throws IOException, InvalidConfigurationException {
        if (configSubFolder.exists()) {
            File[] files = configSubFolder.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(".")) continue;
                if (file.isDirectory()) {
                    config = loadConfigFolder(config, file, setEnabled);
                } else {
                    ConfigurationSection fileOverrides = CompatibilityUtils.loadConfiguration(file);
                    getLogger().info("  Loading " + file.getName());
                    if (setEnabled) {
                        enableAll(fileOverrides);
                    }
                    config = ConfigurationUtils.addConfigurations(config, fileOverrides);
                }
            }
        } else {
            configSubFolder.mkdir();
        }

        return config;
    }

    private ConfigurationSection loadRemoteConfiguration(String urlString) {
        YamlConfiguration configuration = new YamlConfiguration();
        InputStream openStream = null;
        try {
            URL url = new URL(urlString);
            openStream = url.openStream();
            configuration.load(new InputStreamReader(openStream));
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error reading remote configuration: " + urlString, ex);
        } finally {
            if (openStream != null) {
                try {
                    openStream.close();
                } catch (IOException ignore) {
                }
            }
        }

        return configuration;
    }

    @Nullable
    private JsonElement loadJSON(String urlString) {
        JsonElement element = null;
        BufferedReader openStream = null;
        try {
            URL url = new URL(urlString);
            openStream = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = openStream.read(chars)) != -1)
                buffer.append(chars, 0, read);

            JsonParser parser = new JsonParser();
            element = parser.parse(buffer.toString());
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error reading remote file: " + urlString);
        } finally {
            if (openStream != null) {
                try {
                    openStream.close();
                } catch (IOException ignore) {
                }
            }
        }

        return element;
    }

    private ConfigurationSection loadGithubConfiguration(String fileName, String url) {
        ConfigurationSection configuration = new MemoryConfiguration();

        // Fix up URLs
        if (!url.endsWith("/")) {
            url += "/";
        }
        String baseFile = url;
        baseFile = baseFile.replace("github.com", "raw.githubusercontent.com");

        String apiURL = url;
        apiURL = apiURL.replace("github.com", "api.github.com/repos");
        if (url.contains("tree/master")) {
            apiURL = apiURL.replace("tree/master", "contents");
            apiURL = apiURL.replace("tree/master", "master");
        } else {
            apiURL += "contents/";
            baseFile += "master/";
        }
        baseFile += fileName + ".yml";
        apiURL += fileName;

        // First look for a base file
        getLogger().info("Loading " + baseFile);
        ConfigurationSection base = loadRemoteConfiguration(baseFile);
        ConfigurationUtils.addConfigurations(configuration, base);

        // Scan folder for contents
        getLogger().info("  Scanning " + apiURL);
        JsonElement folderContents = loadJSON(apiURL);
        if (folderContents != null) {
            getLogger().info(folderContents.getClass().getName());
            if (folderContents.isJsonObject()) {
                JsonObject object = (JsonObject)folderContents;
                if (object.has("message")) {
                    getLogger().info("     " + object.get("message").getAsString());
                }
            } else if (folderContents.isJsonArray()) {
                JsonArray array = (JsonArray)folderContents;
                getLogger().info("   Size: " + array.size());
                for (int i = 0; i < array.size(); i++) {
                    JsonElement fileElement = array.get(i);
                    getLogger().info(fileElement.getAsString());
                    if (fileElement.isJsonObject()) {
                        JsonObject fileObject = (JsonObject)fileElement;
                        if (fileObject.has("type")) {
                            getLogger().info(fileObject.get("name").getAsString());
                        }
                    }
                }
            }
        }
        return configuration;
    }

    private ConfigurationSection mapSpells(ConfigurationSection spellConfiguration) throws InvalidConfigurationException, IOException {
        ConfigurationSection spellConfigs = new MemoryConfiguration();
        if (spellConfiguration == null) return spellConfigs;

        // Reset cached spell configs
        spellConfigurations.clear();
        baseSpellConfigurations.clear();

        Set<String> spellKeys = spellConfiguration.getKeys(false);
        for (String key : spellKeys) {
            if (key.equals("default") || key.equals("override")) continue;

            ConfigurationSection spellNode = getSpellConfig(key, spellConfiguration);
            if (spellNode == null || !spellNode.getBoolean("enabled", true)) {
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
                    spellNode = ConfigurationUtils.addConfigurations(spellNode, inheritConfig, false);
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
        }

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
                loadedConfigurations.put(configurationFile, new MemoryConfiguration());
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

    public boolean isSuccessful() {
        return success;
    }
}
