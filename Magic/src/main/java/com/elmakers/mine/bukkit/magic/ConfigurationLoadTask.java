package com.elmakers.mine.bukkit.magic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellKey;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ConfigurationLoadTask implements Runnable {
    private final MagicController controller;
    private final File configFolder;
    private final Plugin plugin;
    private final CommandSender sender;

    private static final String SPELLS_FILE         = "spells";
    private static final String CONFIG_FILE         = "config";
    private static final String WANDS_FILE          = "wands";
    private static final String PATHS_FILE          = "paths";
    private static final String CRAFTING_FILE       = "crafting";
    private static final String CLASSES_FILE        = "classes";
    private static final String MESSAGES_FILE       = "messages";
    private static final String MATERIALS_FILE      = "materials";
    private static final String MOBS_FILE           = "mobs";
    private static final String ITEMS_FILE          = "items";
    private static final String ATTRIBUTES_FILE     = "attributes";
    private static final String AUTOMATA_FILE       = "automata";
    private static final String EFFECTS_FILE        = "effects";

    private final Map<String, ConfigurationSection> spellConfigurations     = new HashMap<>();
    private final Map<String, ConfigurationSection> baseSpellConfigurations = new HashMap<>();

    private boolean disableDefaultSpells = false;
    private boolean disableDefaultWands = false;
    private boolean loadDefaultSpells = true;
    private boolean loadDefaultWands = true;
    private boolean loadDefaultPaths = true;
    private boolean loadDefaultCrafting = true;
    private boolean loadDefaultClasses = true;
    private boolean loadDefaultMobs = true;
    private boolean loadDefaultItems = true;
    private boolean loadDefaultAttributes = true;
    private boolean loadDefaultAutomata = true;
    private boolean loadDefaultEffects = true;
    private boolean spellUpgradesEnabled = true;

    private static final Object loadLock = new Object();

    private boolean allPvpRestricted = false;
    private boolean noPvpRestricted = false;
    private boolean saveDefaultConfigs = true;

    private String exampleDefaults = null;
    private Collection<String> addExamples = null;

    private ConfigurationSection configuration;
    private ConfigurationSection messages;
    private ConfigurationSection materials;
    private ConfigurationSection wands;
    private ConfigurationSection paths;
    private ConfigurationSection crafting;
    private ConfigurationSection mobs;
    private ConfigurationSection items;
    private ConfigurationSection classes;
    private ConfigurationSection attributes;
    private ConfigurationSection automata;
    private ConfigurationSection effects;
    private ConfigurationSection spells;

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
    }

    private ConfigurationSection loadMainConfiguration() throws InvalidConfigurationException, IOException {
        ConfigurationSection configuration = loadConfigFile(CONFIG_FILE, true);
        loadInitialProperties(configuration);
        return configuration;
    }

    private ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults, ConfigurationSection mainConfiguration)
        throws IOException, InvalidConfigurationException {
        return loadConfigFile(fileName, loadDefaults, false, mainConfiguration);
    }

    private ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults)
        throws IOException, InvalidConfigurationException {
        return loadConfigFile(fileName, loadDefaults, false);
    }

    private ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults, boolean disableDefaults)
        throws IOException, InvalidConfigurationException {
        return loadConfigFile(fileName, loadDefaults, disableDefaults, null);
    }

    private ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults, boolean disableDefaults, ConfigurationSection mainConfiguration)
        throws IOException, InvalidConfigurationException {
        return loadConfigFile(fileName, loadDefaults, disableDefaults, false, mainConfiguration);
    }

    private ConfigurationSection loadConfigFile(String fileName, boolean loadDefaults, boolean disableDefaults, boolean filesReplace, ConfigurationSection mainConfiguration)
        throws IOException, InvalidConfigurationException {
        String configFileName = fileName + ".yml";
        File configFile = new File(configFolder, configFileName);
        if (!configFile.exists()) {
            getLogger().info("Saving template " + configFileName + ", edit to customize configuration.");
            plugin.saveResource(configFileName, false);
        }

        boolean usingExample = exampleDefaults != null && exampleDefaults.length() > 0;

        String examplesFileName = usingExample ? "examples/" + exampleDefaults + "/" + fileName + ".yml" : null;
        String defaultsFileName = "defaults/" + fileName + ".defaults.yml";

        File savedDefaults = new File(configFolder, defaultsFileName);
        if (saveDefaultConfigs) {
            plugin.saveResource(defaultsFileName, true);
        } else if (savedDefaults.exists()) {
            getLogger().info("Deleting defaults file: " + defaultsFileName + ", these have been removed to avoid confusion");
            savedDefaults.delete();
        }

        getLogger().info("Loading " + configFile.getName());
        ConfigurationSection overrides = CompatibilityUtils.loadConfiguration(configFile);
        ConfigurationSection config = new MemoryConfiguration();

        if (loadDefaults) {
            getLogger().info(" Based on defaults " + defaultsFileName);
            ConfigurationSection defaultConfig = CompatibilityUtils.loadConfiguration(plugin.getResource(defaultsFileName));
            if (disableDefaults) {
                Set<String> keys = defaultConfig.getKeys(false);
                for (String key : keys)
                {
                    defaultConfig.getConfigurationSection(key).set("enabled", false);
                }
                enableAll(overrides);
            }
            config = ConfigurationUtils.addConfigurations(config, defaultConfig);
        }

        if (mainConfiguration != null) {
            config = ConfigurationUtils.addConfigurations(config, mainConfiguration);
        }

        if (usingExample) {
            InputStream input = plugin.getResource(examplesFileName);
            if (input != null)
            {
                ConfigurationSection exampleConfig = CompatibilityUtils.loadConfiguration(input);
                if (disableDefaults) {
                    enableAll(exampleConfig);
                }
                config = ConfigurationUtils.addConfigurations(config, exampleConfig);
                getLogger().info(" Using " + examplesFileName);
            }
        }

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
                    config = ConfigurationUtils.addConfigurations(config, exampleConfig, false);
                    getLogger().info(" Added " + examplesFileName);
                }
            }
        }

        // Apply overrides after loading defaults and examples
        config = ConfigurationUtils.addConfigurations(config, overrides);

        // Apply file overrides last
        File configSubFolder = new File(configFolder, fileName);
        config = loadConfigFolder(config, configSubFolder, filesReplace, disableDefaults);

        return config;
    }

    private void enableAll(ConfigurationSection rootSection) {
        Set<String> keys = rootSection.getKeys(false);
        for (String key : keys)
        {
            ConfigurationSection section = rootSection.getConfigurationSection(key);
            if (!section.isSet("enabled")) {
                section.set("enabled", true);
            }
        }
    }

    private ConfigurationSection loadConfigFolder(ConfigurationSection config, File configSubFolder, boolean filesReplace, boolean setEnabled)
        throws IOException, InvalidConfigurationException {
        if (configSubFolder.exists()) {
            File[] files = configSubFolder.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(".")) continue;
                if (file.isDirectory()) {
                    config = loadConfigFolder(config, file, filesReplace, setEnabled);
                } else {
                    ConfigurationSection fileOverrides = CompatibilityUtils.loadConfiguration(file);
                    getLogger().info("  Loading " + file.getName());
                    if (setEnabled) {
                        enableAll(fileOverrides);
                    }
                    if (filesReplace) {
                        config = ConfigurationUtils.replaceConfigurations(config, fileOverrides);
                    } else {
                        config = ConfigurationUtils.addConfigurations(config, fileOverrides);
                    }
                }
            }
        } else {
            configSubFolder.mkdir();
        }

        return config;
    }

    private ConfigurationSection loadExamples(ConfigurationSection properties) throws InvalidConfigurationException, IOException {
        exampleDefaults = properties.getString("example", exampleDefaults);
        addExamples = properties.getStringList("add_examples");

        if ((exampleDefaults != null && exampleDefaults.length() > 0) || (addExamples != null && addExamples.size() > 0)) {
            // Reload config, example will be used this time.
            if (exampleDefaults != null && exampleDefaults.length() > 0)
            {
                getLogger().info("Overriding configuration with example: " + exampleDefaults);
            }
            if (addExamples != null && addExamples.size() > 0)
            {
                getLogger().info("Adding examples: " + StringUtils.join(addExamples, ","));
            }
            properties = loadConfigFile(CONFIG_FILE, true);
        }

        loadDefaultSpells = properties.getBoolean("load_default_spells", loadDefaultSpells);
        disableDefaultSpells = properties.getBoolean("disable_default_spells", disableDefaultSpells);
        loadDefaultWands = properties.getBoolean("load_default_wands", loadDefaultWands);
        disableDefaultWands = properties.getBoolean("disable_default_wands", disableDefaultWands);
        loadDefaultCrafting = properties.getBoolean("load_default_crafting", loadDefaultCrafting);
        loadDefaultClasses = properties.getBoolean("load_default_classes", loadDefaultClasses);
        loadDefaultPaths = properties.getBoolean("load_default_enchanting", loadDefaultPaths);
        loadDefaultPaths = properties.getBoolean("load_default_paths", loadDefaultPaths);
        loadDefaultMobs = properties.getBoolean("load_default_mobs", loadDefaultMobs);
        loadDefaultItems = properties.getBoolean("load_default_items", loadDefaultItems);
        loadDefaultAttributes = properties.getBoolean("load_default_attributes", loadDefaultAttributes);
        loadDefaultAutomata = properties.getBoolean("load_default_automata", loadDefaultAutomata);
        loadDefaultEffects = properties.getBoolean("load_default_effects", loadDefaultEffects);

        if (!properties.getBoolean("load_default_configs")) {
            loadDefaultWands = false;
            loadDefaultCrafting = false;
            loadDefaultClasses = false;
            loadDefaultPaths = false;
            loadDefaultMobs = false;
            loadDefaultItems = false;
            loadDefaultSpells = false;
            loadDefaultAttributes = false;
            loadDefaultAutomata = false;
            loadDefaultEffects = false;
        }

        return properties;
    }

    private ConfigurationSection loadMessageConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(MESSAGES_FILE, true, mainConfiguration.getConfigurationSection("messages"));
    }

    private ConfigurationSection loadMaterialsConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(MATERIALS_FILE, true, mainConfiguration.getConfigurationSection("materials"));
    }

    private ConfigurationSection loadWandConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(WANDS_FILE, loadDefaultWands, disableDefaultWands, true, mainConfiguration.getConfigurationSection("wands"));
    }

    private ConfigurationSection loadPathConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(PATHS_FILE, loadDefaultPaths, mainConfiguration.getConfigurationSection("paths"));
    }

    private ConfigurationSection loadCraftingConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(CRAFTING_FILE, loadDefaultCrafting, mainConfiguration.getConfigurationSection("crafting"));
    }

    private ConfigurationSection loadClassConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(CLASSES_FILE, loadDefaultClasses, mainConfiguration.getConfigurationSection("classes"));
    }

    private ConfigurationSection loadMobsConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(MOBS_FILE, loadDefaultMobs, mainConfiguration.getConfigurationSection("mobs"));
    }

    private ConfigurationSection loadItemsConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(ITEMS_FILE, loadDefaultItems, mainConfiguration.getConfigurationSection("items"));
    }

    private ConfigurationSection loadAttributesConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(ATTRIBUTES_FILE, loadDefaultAttributes, mainConfiguration.getConfigurationSection("attributes"));
    }

    private ConfigurationSection loadAutomataConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(AUTOMATA_FILE, loadDefaultAutomata, mainConfiguration.getConfigurationSection("automata"));
    }

    private ConfigurationSection loadEffectConfiguration(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        return loadConfigFile(EFFECTS_FILE, loadDefaultEffects, mainConfiguration.getConfigurationSection("effects"));
    }

    private ConfigurationSection loadAndMapSpells(ConfigurationSection mainConfiguration) throws InvalidConfigurationException, IOException {
        ConfigurationSection spellConfigs = new MemoryConfiguration();
        ConfigurationSection config = loadConfigFile(SPELLS_FILE, loadDefaultSpells, disableDefaultSpells, mainConfiguration.getConfigurationSection("spells"));
        if (config == null) return spellConfigs;

        // Reset cached spell configs
        spellConfigurations.clear();
        baseSpellConfigurations.clear();

        Set<String> spellKeys = config.getKeys(false);
        for (String key : spellKeys) {
            if (key.equals("default") || key.equals("override")) continue;

            ConfigurationSection spellNode = getSpellConfig(key, config);
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
            configuration = loadMainConfiguration();
            configuration = loadExamples(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading config.yml", ex);
            success = false;
        }

        // Load messages
        try {
            messages = loadMessageConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading messages.yml", ex);
            success = false;
        }

        // Load materials configuration
        try {
            materials = loadMaterialsConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading material.yml", ex);
            success = false;
        }

        // Load spells, and map their inherited configs
        try {
            spells = loadAndMapSpells(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading spells.yml", ex);
            success = false;
        }

        // Load enchanting paths
        try {
            paths = loadPathConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading paths.yml", ex);
            success = false;
        }

        // Load wand templates
        try {
            wands = loadWandConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading wands.yml", ex);
            success = false;
        }

        // Load crafting recipes
        try {
            crafting = loadCraftingConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading crafting.yml", ex);
            success = false;
        }


        // Load classes
        try {
            classes = loadClassConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading classes.yml", ex);
            success = false;
        }

        // Load mobs
        try {
            mobs = loadMobsConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading mobs.yml", ex);
            success = false;
        }

        // Load items
        try {
            items = loadItemsConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading items.yml", ex);
            success = false;
        }

        // Load attributes
        try {
            attributes = loadAttributesConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading attributes.yml", ex);
            success = false;
        }

        // Load automata
        try {
            automata = loadAutomataConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading automata.yml", ex);
            success = false;
        }

        // Load effects
        try {
            effects = loadEffectConfiguration(configuration);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading effects.yml", ex);
            success = false;
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

    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    public ConfigurationSection getMessages() {
        return messages;
    }

    public ConfigurationSection getMaterials() {
        return materials;
    }

    public ConfigurationSection getWands() {
        return wands;
    }

    public ConfigurationSection getPaths() {
        return paths;
    }

    public ConfigurationSection getCrafting() {
        return crafting;
    }

    public ConfigurationSection getMobs() {
        return mobs;
    }

    public ConfigurationSection getItems() {
        return items;
    }

    public ConfigurationSection getClasses() {
        return classes;
    }

    public ConfigurationSection getAttributes() {
        return attributes;
    }

    public ConfigurationSection getAutomata() {
        return automata;
    }

    public ConfigurationSection getEffects() {
        return effects;
    }

    public ConfigurationSection getSpells() {
        return spells;
    }

    public boolean isSuccessful() {
        return success;
    }
}
