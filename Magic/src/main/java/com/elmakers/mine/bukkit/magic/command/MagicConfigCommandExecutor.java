package com.elmakers.mine.bukkit.magic.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.WandTemplate;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.command.config.ApplySessionCallback;
import com.elmakers.mine.bukkit.magic.command.config.AsyncProcessor;
import com.elmakers.mine.bukkit.magic.command.config.FetchExampleRunnable;
import com.elmakers.mine.bukkit.magic.command.config.GetSessionRequest;
import com.elmakers.mine.bukkit.magic.command.config.GetSessionRunnable;
import com.elmakers.mine.bukkit.magic.command.config.NewSessionCallback;
import com.elmakers.mine.bukkit.magic.command.config.NewSessionRequest;
import com.elmakers.mine.bukkit.magic.command.config.NewSessionRunnable;
import com.elmakers.mine.bukkit.magic.command.config.Session;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.wand.Wand;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;
import com.google.gson.Gson;

public class MagicConfigCommandExecutor extends MagicTabExecutor {
    private static final String CUSTOM_FILE_NAME = "_customizations.yml";
    private static final String EXAMPLES_FILE_NAME = "_examples.yml";
    private static Set<String> exampleActions = ImmutableSet.of("add", "remove", "set", "list", "fetch", "help", "unfetch");
    protected static Set<String> availableFiles = ImmutableSet.of(
            "spells", "wands", "blocks", "classes", "config", "crafting", "effects", "items",
            "kits", "materials", "mobs", "paths", "attributes", "messages", "modifiers", "worlds",
            "arenas", "icons");
    protected static final Map<String, String> availableFileMap = ImmutableMap.<String, String>builder()
        .put("spell", "spells")
        .put("wand", "wands")
        .put("block", "blocks")
        .put("class", "classes")
        .put("recipe", "crafting")
        .put("effect", "effects")
        .put("item", "items")
        .put("kit", "kits")
        .put("material", "materials")
        .put("mob", "mobs")
        .put("path", "paths")
        .put("attribute", "attributes")
        .put("message", "messages")
        .put("modifier", "modifiers")
        .put("world", "worlds")
        .put("config", "config")
        .put("arena", "arenas")
        .put("icon", "icons")
        .build();

    private final MagicController magic;
    private Gson gson;
    private final Map<String, String> sessions = new HashMap<>();

    protected MagicConfigCommandExecutor(MagicAPI api, MagicController controller, String command) {
        super(api, command);
        this.magic = controller;
    }

    public MagicConfigCommandExecutor(MagicAPI api, MagicController controller) {
        this(api, controller, "mconfig");
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("magic.commands.mconfig")) return options;

        if (args.length == 1) {
            addIfPermissible(sender, options, "magic.commands.mconfig.", "clean");
            addIfPermissible(sender, options, "magic.commands.mconfig.", "disable");
            addIfPermissible(sender, options, "magic.commands.mconfig.", "enable");
            addIfPermissible(sender, options, "magic.commands.mconfig.", "configure");
            addIfPermissible(sender, options, "magic.commands.mconfig.", "reset");
            addIfPermissible(sender, options, "magic.commands.mconfig.", "editor");
            addIfPermissible(sender, options, "magic.commands.mconfig.", "load");
            addIfPermissible(sender, options, "magic.commands.mconfig.", "apply");
            addIfPermissible(sender, options, "magic.commands.mconfig.", "example");
            addIfPermissible(sender, options, "magic.commands.mconfig.", "language");
            if (api.hasPermission(sender, "magic.commands.mconfig.configure")) {
                for (String fileType : availableFileMap.keySet()) {
                    options.add(fileType);
                }
            }
        }
        String subCommand = args[0];

        // Handle shortcuts for config
        if (availableFileMap.containsKey(subCommand) || availableFiles.contains(subCommand)) {
            subCommand = "configure";
            args = ObjectArrays.concat("configure", args);
        }

        if (subCommand.equals("example")) {
            args = Arrays.copyOfRange(args, 1, args.length);
            addExampleTabComplete(args, options);
            return options;
        }
        if (args.length == 2 && (subCommand.equals("disable") || subCommand.equals("enable") || subCommand.equals("configure") || subCommand.equals("editor") || subCommand.equals("reset"))) {
            options.addAll(availableFileMap.keySet());
            if (subCommand.equals("configure") || subCommand.equals("reset")) {
                options.add("config");
            }
            if (subCommand.equals("editor") || subCommand.equals("reset")) {
                options.remove("message");
                options.add("messages");
            }
        }

        if (args.length == 2 && subCommand.equals("language")) {
            options.addAll(controller.getLocalizations());
        }

        // After here we assume args[1] is the file type
        String fileType = args.length < 2 ? null : getFileParameter(args[1]);
        if (fileType == null) {
            return options;
        }

        // Spell configuration keys and parameters
        if (subCommand.equals("configure")) {
            if ((args.length == 4 || args.length == 5) && fileType.equals("spells")) {
                if (fileType.equals("spells")) {
                    String spellName = args[2];
                    SpellTemplate spell = api.getSpellTemplate(spellName);
                    if (spell != null) {
                        if (args.length == 4) {
                            Collection<String> parameters = new ArrayList<>();
                            spell.getParameters(parameters);
                            for (String parameter : parameters) {
                                options.add("parameters." + parameter);
                            }
                            options.add("icon");
                            options.add("upgrade_required_path");
                            options.add("upgrade_required_casts");
                            options.add("icon_disabled");
                            options.add("color");
                            options.add("costs");
                            options.add("worth");
                            options.add("earns_sp");
                            options.add("earns_cooldown");
                            options.add("require_health_percentage");
                            options.add("quick_cast");
                            options.add("pvp_restricted");
                            options.add("passive");
                            options.add("cast_on_no_target");
                            options.add("undoable");
                        } else {
                            String parameter = args[args.length - 2];
                            String prefix = "parameters.";
                            if (parameter.startsWith(prefix)) {
                                spell.getParameterOptions(options, parameter.substring(prefix.length()));
                            }
                        }
                    }
                }
            }

            // Wand configuration keys and parameters
            if (args.length == 4 && fileType.equals("wands")) {
                Wand.addParameterKeys(api.getController(), options);
            }
            if (args.length == 5 && fileType.equals("wands")) {
                Wand.addParameterValues(api.getController(), args[3], options);
            }

            // Special-case config options
            if (fileType.equals("config") && args.length == 4 && args[2].equals("language")) {
                options.addAll(controller.getLocalizations());
            }
        }

        // Handle common editor/enable/disable/configure keys all together
        if (args.length == 3 && (
               subCommand.equals("disable")
            || subCommand.equals("configure")
            || subCommand.equals("editor")
            || subCommand.equals("reset"))) {

            if ((subCommand.equals("configure") || subCommand.equals("reset")) && fileType.equals("config")) {
                options.add("language");
                File pluginFolder = api.getPlugin().getDataFolder();
                File defaultsFile = new File(pluginFolder, "defaults/config.defaults.yml");
                YamlConfiguration defaultConfig = null;
                try {
                    defaultConfig = new YamlConfiguration();
                    defaultConfig.load(defaultsFile);
                    options.addAll(defaultConfig.getKeys(false));
                } catch (Exception ignore) {
                }
            }

            if (subCommand.equals("configure") && fileType.equals("messages")) {
                options.addAll(controller.getMessages().getAllKeys());
            }
            if (fileType.equals("worlds") && subCommand.equals("configure")) {
                for (World world : api.getPlugin().getServer().getWorlds()) {
                    options.add(world.getName());
                }
            }

            addConfigureOptions(fileType, options);
        }
        return options;
    }

    protected void addExampleTabComplete(String[] args, Set<String> options) {
        if (args.length < 2) {
            options.addAll(exampleActions);
            return;
        }

        String operation = args[0];
        if (operation.equals("add")) {
            options.addAll(controller.getExamples());
        } else if (args.length > 2 && args[0].equals("set") && !args[1].equals("none")) {
            if (args.length == 3) {
                options.add("none");
            }
            options.addAll(controller.getExamples());
        } else if (args.length == 2) {
            if (operation.equals("fetch") || operation.equals("unfetch")) {
                options.addAll(controller.getExternalExamples());
            } else if (operation.equals("remove")) {
                options.addAll(controller.getLoadedExamples());
                options.add("all");
            } else if (operation.equals("set")) {
                options.add("none");
                options.addAll(controller.getExamples());
            }
        }
    }

    protected void addConfigureOptions(String fileType, Collection<String> options) {
        if (fileType.equals("worlds")) {
            File pluginFolder = api.getPlugin().getDataFolder();
            File defaultsFile = new File(pluginFolder, "defaults/worlds.defaults.yml");
            YamlConfiguration defaultConfig = null;
            try {
                defaultConfig = new YamlConfiguration();
                defaultConfig.load(defaultsFile);
                options.addAll(defaultConfig.getKeys(false));
            } catch (Exception ignore) {
            }
        }
        if (fileType.equals("arenas")) {
            Collection<String> arenaKeys = api.getController().getArenaTemplateKeys();
            options.addAll(arenaKeys);
        }
        if (fileType.equals("icons")) {
            Collection<String> iconKeys = api.getController().getIconKeys();
            options.addAll(iconKeys);
        }
        if (fileType.equals("spells")) {
            Collection<SpellTemplate> spellList = api.getController().getSpellTemplates(true);
            for (SpellTemplate spell : spellList) {
                options.add(spell.getKey());
            }
            // Some special cases
            options.add("default");
            options.add("override");
        }
        if (fileType.equals("wands")) {
            Collection<WandTemplate> wandList = api.getController().getWandTemplates();
            for (WandTemplate wand : wandList) {
                options.add(wand.getKey());
            }
        }
        if (fileType.equals("paths")) {
            Collection<String> pathList = api.getController().getWandPathKeys();
            for (String path : pathList) {
                options.add(path);
            }
        }
        if (fileType.equals("modifiers")) {
            Collection<String> modifierList = api.getController().getModifierTemplateKeys();
            for (String modifier : modifierList) {
                options.add(modifier);
            }
        }
        if (fileType.equals("crafting")) {
            Collection<String> recipeList = api.getController().getRecipeKeys();
            for (String recipe : recipeList) {
                options.add(recipe);
            }
        }
        if (fileType.equals("mobs")) {
            Collection<String> mobList = api.getController().getMobKeys(true);
            for (String mob : mobList) {
                options.add(mob);
            }
        }
        if (fileType.equals("items")) {
            Collection<String> itemList = api.getController().getItemKeys();
            for (String item : itemList) {
                options.add(item);
            }
        }
        if (fileType.equals("blocks")) {
            Collection<String> list = api.getController().getMagicBlockTemplateKeys();
            for (String key : list) {
                options.add(key);
            }
        }
        if (fileType.equals("classes")) {
            Collection<String> list = api.getController().getMageClassKeys();
            for (String key : list) {
                options.add(key);
            }
        }
        if (fileType.equals("attributes")) {
            Collection<String> list = api.getController().getAttributes();
            for (String key : list) {
                options.add(key);
            }
        }
        if (fileType.equals("effects")) {
            Collection<String> list = api.getController().getEffectKeys();
            for (String key : list) {
                options.add(key);
            }
        }
        if (fileType.equals("kits")) {
            Collection<String> list = api.getController().getKitKeys();
            for (String key : list) {
                options.add(key);
            }
        }
        if (fileType.equals("materials")) {
            Collection<String> list = api.getController().getMaterialSetManager().getMaterialSets();
            for (String key : list) {
                if (!key.startsWith("!")) {
                    options.add(key);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0];

        // Handle shortcuts for config
        if (availableFileMap.containsKey(subCommand) || availableFiles.contains(subCommand)) {
            subCommand = "configure";
            args = ObjectArrays.concat("configure", args);
        }

        if (!api.hasPermission(sender, "magic.commands.mconfig." + subCommand)) {
            sendNoPermission(sender);
            return true;
        }

        if (subCommand.equals("clean")) {
            onMagicClean(sender, args.length > 1 ? args[1] : "");
            return true;
        }
        String[] parameters = Arrays.copyOfRange(args, 1, args.length);
        if (subCommand.equals("enable")) {
            onMagicEnable(sender, parameters);
            return true;
        }
        if (subCommand.equals("disable")) {
            onMagicDisable(sender, parameters);
            return true;
        }
        if (subCommand.equals("configure")) {
            onMagicConfigure(sender, parameters);
            return true;
        }
        if (subCommand.equals("language")) {
            onMagicLanguage(sender, parameters);
            return true;
        }
        if (subCommand.equals("reset")) {
            onReset(sender, parameters);
            return true;
        }
        if (subCommand.equals("editor")) {
            onStartEditor(sender, parameters);
            return true;
        }
        if (subCommand.equals("load")) {
            onApplyEdits(sender, parameters, "load", true);
            return true;
        }
        if (subCommand.equals("apply")) {
            onApplyEdits(sender, parameters, "apply", false);
            return true;
        }
        if (subCommand.equals("example")) {
            onExample(sender, parameters);
            return true;
        }
        return false;
    }

    protected String getFileParameter(String fileKey) {
        if (availableFiles.contains(fileKey)) {
            return fileKey;
        }
        return availableFileMap.get(fileKey);
    }

    protected String escapeMessage(String message, String type, String key) {
        return escapeMessage(message, type, key, ' ');
    }

    protected String escapeMessage(String message, String type, String key, char delimiter) {
        Set<String> options = new HashSet<>(availableFileMap.keySet());
        options.add("config");
        return message.replace("$type", type)
                .replace("$key", key)
                .replace("$options", StringUtils.join(options, delimiter));
    }

    @Nullable
    protected File getConfigFile(CommandSender sender, String command, String[] parameters) {
        if (parameters.length < 1) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig." + command + ".usage"), "", "", '|'));
            return null;
        }
        String fileKey = getFileParameter(parameters[0]);
        if (fileKey == null) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig." + command + ".nokey"), "", "", ','));
            return null;
        }
        if (!fileKey.equals("config") && !fileKey.equals("messages") && parameters.length < 2) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig." + command + ".usage"), "", "", '|'));
            return null;
        }

        return getConfigFile(fileKey);
    }

    protected File getConfigFile(String fileKey) {
        return new File(magic.getPlugin().getDataFolder() + File.separator + fileKey, CUSTOM_FILE_NAME);
    }

    protected void trySave(String command, CommandSender sender, File configFile, YamlConfiguration configuration, String fileKey, String key) {
        try {
            configuration.save(configFile);
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig." + command + ".success"), fileKey, key));
        } catch (Exception ex) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.write_failed").replace("$file", configFile.getName()));
            magic.getLogger().log(Level.SEVERE, "Could not write to file " + configFile.getAbsoluteFile(), ex);
        }
    }

    protected void setPath(ConfigurationSection config, String path, Object value) {
        String[] pieces = StringUtils.split(path);
        for (int i = 0; i < pieces.length - 1; i++) {
            config = config.createSection(pieces[i]);
        }
        config.set(pieces[pieces.length - 1], value);
    }

    protected void onMagicDisable(CommandSender sender, String[] parameters) {
        File configFile = getConfigFile(sender, "disable", parameters);
        if (configFile == null) {
            return;
        }
        String key = parameters[1];
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        setPath(configuration, key + ".enabled", false);
        trySave("disable", sender, configFile, configuration, parameters[0], key);
    }

    protected void onMagicEnable(CommandSender sender, String[] parameters) {
        File configFile = getConfigFile(sender, "enable", parameters);
        if (configFile == null) {
            return;
        }
        String key = parameters[1];
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        setPath(configuration, key + ".enabled", true);
        trySave("enable", sender, configFile, configuration, parameters[0], key);
    }

    protected Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    protected void onStartEditor(CommandSender sender, String[] parameters) {
        String editorType = "config";
        if (parameters.length > 0) {
            editorType = parameters[0];
        }
        editorType = getFileParameter(editorType);
        if (editorType == null) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig.editor.usage"), "", "", '|'));
            return;
        }

        // Build session request
        NewSessionRequest newSession = new NewSessionRequest(controller, editorType);
        if (sender instanceof Player) {
            newSession.setBukkitPlayer((Player)sender);
        }
        newSession.setLegacyIcons(magic.isLegacyIconsEnabled());
        newSession.setMagicVersion(getMagicVersion());
        newSession.setMinecraftVersion(CompatibilityLib.getServerVersion(api.getPlugin()));

        if ((editorType.equals("config") || editorType.equals("messages")) && parameters.length <= 1) {
            File pluginFolder = api.getPlugin().getDataFolder();
            File targetFile = new File(pluginFolder, editorType + ".yml");
            if (targetFile.exists()) {
                YamlConfiguration testConfig = new YamlConfiguration();
                try {
                    testConfig.load(targetFile);
                    if (testConfig.getKeys(false).isEmpty()) {
                        targetFile = null;
                    }
                } catch (Exception ex) {
                    sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.error"));
                    magic.getLogger().log(Level.WARNING, "Error loading customized file: " + targetFile.getAbsolutePath(), ex);
                    return;
                }
            }
            String defaultConfig = null;
            if (targetFile == null || !targetFile.exists()) {
                targetFile = new File(pluginFolder, "defaults/" + editorType + ".defaults.yml");
            }
            try {
                defaultConfig = new String(Files.readAllBytes(Paths.get(targetFile.getAbsolutePath())), StandardCharsets.UTF_8);
            } catch (Exception ex) {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.error"));
                magic.getLogger().log(Level.WARNING, "Error loading customized file: " + targetFile.getAbsolutePath(), ex);
                return;
            }
            newSession.setContents(defaultConfig);
        } else if (parameters.length > 1) {
            String targetItem = parameters[1];
            newSession.setKey(targetItem);

            File pluginFolder = api.getPlugin().getDataFolder();
            File customFolder = new File(pluginFolder, editorType);
            File customFile = new File(customFolder, targetItem + ".yml");
            String existingConfig = null;
            if (customFile.exists()) {
                try {
                    existingConfig = new String(Files.readAllBytes(Paths.get(customFile.getAbsolutePath())), StandardCharsets.UTF_8);
                } catch (Exception ex) {
                    sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.error"));
                    magic.getLogger().log(Level.WARNING, "Error loading customized file: " + customFile.getAbsolutePath(), ex);
                    return;
                }
            } else {
                File defaultsFile = new File(pluginFolder, "defaults/" + editorType + ".defaults.yml");
                YamlConfiguration defaultConfig = null;
                try {
                    defaultConfig = new YamlConfiguration();
                    defaultConfig.load(defaultsFile);
                } catch (Exception ex) {
                    sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.error"));
                    magic.getLogger().log(Level.WARNING, "Error loading default " + editorType + " file", ex);
                    return;
                }

                Plugin plugin = controller.getPlugin();
                ConfigurationSection targetConfig = defaultConfig.getConfigurationSection(targetItem);
                boolean isInheritSet = false;
                if (targetConfig != null) {
                    YamlConfiguration yaml = new YamlConfiguration();
                    yaml.set(targetItem, targetConfig);

                    // Special case here to keep the comments on builtin example files
                    Collection<String> examples = controller.getLoadedExamples();
                    for (String example : examples) {
                        try {
                            InputStream resource = plugin.getResource("examples/" + example + "/" + editorType + ".yml");
                            List<String> fullConfig = new BufferedReader(
                                  new InputStreamReader(resource, StandardCharsets.UTF_8))
                                    .lines()
                                    .collect(Collectors.toList());
                            StringBuilder testConfig = null;
                            StringBuilder header = new StringBuilder();
                            for (String line : fullConfig) {
                                if (testConfig == null) {
                                    if (line.equals(targetItem + ":")) {
                                        testConfig = header;
                                        testConfig.append(line);
                                    } else if (line.startsWith("#")) {
                                        header.append(line);
                                        header.append("\n");
                                    } else {
                                        header.setLength(0);
                                    }
                                } else if (!line.isEmpty() && line.charAt(0) != ' ') {
                                    break;
                                } else {
                                    testConfig.append('\n');
                                    testConfig.append(line);
                                }
                            }
                            if (testConfig != null) {
                                String testConfigString = testConfig.toString();
                                YamlConfiguration testMatch = new YamlConfiguration();
                                testMatch.loadFromString(testConfigString);
                                testMatch.options().header(null);
                                ConfigurationSection mainSection = testMatch.getConfigurationSection(targetItem);
                                isInheritSet = mainSection != null && mainSection.contains("inherit");
                                if (testMatch.saveToString().equals(yaml.saveToString())) {
                                    existingConfig = testConfigString;
                                    break;
                                }
                            }
                        } catch (Exception ignore) {

                        }
                    }
                    if (existingConfig == null) {
                        ConfigurationSection mainSection = yaml.getConfigurationSection(targetItem);
                        isInheritSet = mainSection != null && mainSection.contains("inherit");
                        existingConfig = yaml.saveToString();
                    }
                    if (!isInheritSet && !editorType.equals("config") && !editorType.equals("messages")) {
                        List<String> newLines = new ArrayList<>();
                        String[] lines = StringUtils.split(existingConfig, "\n");
                        for (int i = 0; i < lines.length; i++) {
                            String line = lines[i];
                            newLines.add(line);
                            if (isInheritSet) continue;
                            if (!line.isEmpty() && line.charAt(0) != ' ' && line.charAt(0) != '#') {
                                for (int j = i + 1; j < lines.length; j++) {
                                    String nextLine = lines[j];
                                    if (nextLine.isEmpty() || nextLine.charAt(0) == '#') continue;
                                    char[] characters = nextLine.toCharArray();
                                    int indent = 0;
                                    for (char character : characters) {
                                        if (character != ' ') break;
                                        indent++;
                                    }
                                    newLines.add(nextLine.substring(0, indent) + "# This has been added automatically so that anything you remove here does not get inherited back in from the default configs");
                                    newLines.add(nextLine.substring(0, indent) + "inherit: false");
                                    isInheritSet = true;
                                    break;
                                }
                            }
                        }
                        existingConfig = StringUtils.join(newLines, "\n");
                    }
                } else {
                    // Effect configurations are a list.
                    List<?> listConfig = defaultConfig.getList(targetItem);
                    if (listConfig != null) {
                        YamlConfiguration yaml = new YamlConfiguration();
                        yaml.set(targetItem, listConfig);
                        existingConfig = yaml.saveToString();
                    }
                }
            }
            if (existingConfig == null && editorType.equals("config")) {
                // Lists are saved in a separate file to avoid editing them when using `/mconfig editor config`, but
                // if asking for a specific key, we should allow it.
                File defaultsFile = new File(pluginFolder, "defaults/lists.defaults.yml");
                YamlConfiguration defaultConfig = null;
                try {
                    defaultConfig = new YamlConfiguration();
                    defaultConfig.load(defaultsFile);
                } catch (Exception ex) {
                    sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.error"));
                    magic.getLogger().log(Level.WARNING, "Error loading default config lists file", ex);
                    return;
                }
                List<?> listConfig = defaultConfig.getList(targetItem);
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.set(targetItem, listConfig);
                existingConfig = yaml.saveToString();
            }
            if (existingConfig == null) {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.new_item")
                    .replace("$type", editorType)
                    .replace("$item", targetItem));
            } else {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.edit_item")
                    .replace("$type", editorType)
                    .replace("$item", targetItem));
                newSession.setContents(existingConfig);
            }
        }

        // Send request
        sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.wait"));
        final Plugin plugin = magic.getPlugin();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new NewSessionRunnable(magic, getGson(), sender,
                newSession, new NewSessionCallback() {
            @Override
            public void success(String session) {
                setSession(sender, session);
            }
        }));
    }

    /**
     * Note that this gets called asynchronously
     */
    protected void setSession(CommandSender sender, String session) {
        final Plugin plugin = magic.getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                Mage mage = controller.getMage(sender);
                sessions.put(mage.getId(), session);
            }
        });
    }

    protected void onApplyEdits(CommandSender sender, String[] parameters, String command, boolean load) {
        String sessionId = null;
        if (parameters.length > 0) {
            sessionId = parameters[0];
        } else {
            Mage mage = controller.getMage(sender);
            sessionId = sessions.get(mage.getId());
        }
        if (sessionId == null) {
            if (load) {
                magic.loadConfiguration(sender);
            } else {
                sender.sendMessage(magic.getMessages().get("commands.mconfig." + command + ".usage"));
            }
            return;
        }

        // Build session request
        GetSessionRequest getSession = new GetSessionRequest(sessionId);

        // Send request
        sender.sendMessage(magic.getMessages().get("commands.mconfig." + command + ".wait"));
        final Plugin plugin = magic.getPlugin();
        final String finalSessionId = sessionId;
        ApplySessionCallback callback = new ApplySessionCallback() {
            @Override
            public void success(Session session) {
                applySession(finalSessionId, session, sender, command, load);
            }
        };
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new GetSessionRunnable(magic, getGson(), sender, getSession, callback));
    }

    /**
     * Note that this gets called asynchronously
     */
    protected void applySession(String sessionId, Session session, CommandSender sender, String command, boolean load) {
        String missingMessage = magic.getMessages().get("commands.mconfig." + command + ".missing");
        String type = session.getType();
        if (type == null || type.isEmpty()) {
            missingMessage = missingMessage.replace("$field", "type");
            AsyncProcessor.fail(controller, sender, missingMessage);
            return;
        }

        boolean isMainConfiguration = type.equals("config");
        boolean isMessagesConfiguration = type.equals("messages");
        String key = session.getKey();
        if (!isMainConfiguration && !isMessagesConfiguration && (key == null || key.isEmpty())) {
            missingMessage = missingMessage.replace("$field", "key");
            AsyncProcessor.fail(controller, sender, missingMessage);
            return;
        }
        if (key != null && !key.isEmpty()) {
            isMainConfiguration = false;
            isMessagesConfiguration = false;
        }
        String contents = session.getContents();
        if (contents == null || contents.isEmpty()) {
            missingMessage = missingMessage.replace("$field", "contents");
            AsyncProcessor.fail(controller, sender, missingMessage);
            return;
        }
        YamlConfiguration testLoad = new YamlConfiguration();
        try {
            testLoad.loadFromString(contents);
        } catch (InvalidConfigurationException e) {
            String message = magic.getMessages().get("commands.mconfig." + command + ".invalid");
            AsyncProcessor.fail(controller, sender, message);
            return;
        }

        File file;
        if (isMainConfiguration || isMessagesConfiguration) {
            file = new File(magic.getPlugin().getDataFolder(), type + ".yml");
        } else {
            String filename = key + ".yml";
            filename = filename.replace("|", "_");
            File typeFolder = new File(magic.getPlugin().getDataFolder(), type);
            if (!typeFolder.exists()) {
                typeFolder.mkdir();
            }
            file = new File(typeFolder, filename);
        }
        if (file.exists()) {
            String message = magic.getMessages().get("commands.mconfig." + command + ".overwrote");
            AsyncProcessor.success(controller, sender, message.replace("$file", file.getName()));
        } else {
            String message = magic.getMessages().get("commands.mconfig." + command + ".created");
            AsyncProcessor.success(controller, sender, message.replace("$file", file.getName()));
        }

        try {
            PrintWriter out = new PrintWriter(file, "UTF-8");
            out.print(contents);
            out.close();
            if (load) {
                Plugin plugin = controller.getPlugin();
                plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        controller.loadConfigurationQuietly(sender);
                    }
                });
            } else {
                AsyncProcessor.success(controller, sender, magic.getMessages().get("commands.mconfig." + command + ".load_prompt"));
            }

            final Plugin plugin = magic.getPlugin();
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Mage mage = controller.getMage(sender);
                    sessions.put(mage.getId(), sessionId);
                }
            });
        } catch (Exception ex) {
            String message = magic.getMessages().get("commands.mconfig." + command + ".error_saving");
            AsyncProcessor.fail(controller, sender, message.replace("$file", file.getName()),
        "Error writing config file " + file.getAbsolutePath(), ex);
        }
    }

    protected void onExample(CommandSender sender, String[] parameters) {
        String action = parameters.length == 0 ? null : parameters[0];
        if (action == null || !exampleActions.contains(action)) {
            String message = magic.getMessages().get("commands.mconfig.example.usage");
            message = message.replace("$actions", StringUtils.join(exampleActions, '|'));
            sender.sendMessage(message);
            return;
        }

        parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
        if (action.equals("add")) {
            onAddExample(sender, parameters);
        } else if (action.equals("remove")) {
            onRemoveExample(sender, parameters);
        } else if (action.equals("set")) {
            onSetExample(sender, parameters);
        } else if (action.equals("fetch")) {
            onFetchExample(sender, parameters);
        } else if (action.equals("unfetch")) {
            onUnFetchExample(sender, parameters);
        } else if (action.equals("list")) {
            onListExamples(sender);
        } else if (action.equals("help")) {
            onExampleHelp(sender);
        } else {
            controller.getLogger().warning("Did not handle an example action that is in the set: " + action);
        }
    }

    protected void onExampleHelp(CommandSender sender) {
        magic.showExampleInstructions(sender);
    }

    protected void onListExamples(CommandSender sender) {
        String baseExample = controller.getExample();
        Set<String> examples = new HashSet<>(controller.getLoadedExamples());
        if (baseExample != null && !baseExample.isEmpty()) {
            examples.remove(baseExample);
            sender.sendMessage(magic.getMessages().get("commands.mconfig.example.list.base").replace("$example", baseExample));
        }
        sender.sendMessage(magic.getMessages().get("commands.mconfig.example.list.header"));
        for (String example : examples) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.example.list.item").replace("$example", example));
        }
    }

    protected void onAddExample(CommandSender sender, String[] parameters) {
        if (parameters.length == 0) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.example.add.usage"));
            return;
        }
        Set<String> examples = new LinkedHashSet<>(controller.getLoadedExamples());
        List<String> addExamples = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            addExamples.add(parameters[i]);
        }
        if (examples.containsAll(addExamples)) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.example.add.duplicate").replace("$examples", StringUtils.join(addExamples, ",")));
            return;
        }
        examples.addAll(addExamples);
        if (configureExamples(sender, examples, controller.getExample())) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.example.add.success").replace("$examples", StringUtils.join(addExamples, ",")));
        }
    }

    protected void onRemoveExample(CommandSender sender, String[] parameters) {
        if (parameters.length == 0) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.example.remove.usage"));
            return;
        }
        String example = parameters[0];
        if (example.equalsIgnoreCase("all")) {
            if (configureExamples(sender, new HashSet<>(), null, false)) {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.example.remove.all"));
            }
            return;
        }
        Set<String> examples = new LinkedHashSet<>(controller.getLoadedExamples());
        if (!examples.contains(example)) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.example.remove.missing").replace("$example", example));
            return;
        }
        String currentExample = controller.getExample();
        examples.remove(example);
        if (currentExample != null && currentExample.equals(example)) {
            currentExample = null;
        }
        if (configureExamples(sender, examples, currentExample)) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.example.remove.success").replace("$example", example));
        }
    }

    protected void onFetchExample(CommandSender sender, String[] parameters) {
        final Plugin plugin = magic.getPlugin();
        if (parameters.length < 1) {
            Collection<String> externalKeys = magic.getDownloadedExternalExamples();
            if (externalKeys.isEmpty()) {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.example.fetch.usage"));
                return;
            }
            magic.updateExternalExamples(sender);
            return;
        }
        String exampleKey = parameters[0];
        String url;
        if (parameters.length < 2) {
            url = controller.getExternalExampleURL(exampleKey);
            if (url == null || url.isEmpty()) {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.example.fetch.unknown").replace("$example", exampleKey));
                return;
            }
        } else {
            url = parameters[1];
        }

        sender.sendMessage(magic.getMessages().get("commands.mconfig.example.fetch.wait").replace("$url", url));
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new FetchExampleRunnable(magic, sender, exampleKey, url));
    }

    protected void onUnFetchExample(CommandSender sender, String[] parameters) {
        if (parameters.length < 1) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.example.unfetch.usage"));
            return;
        }
        String exampleKey = parameters[0];
        File examplesFolder = new File(controller.getPlugin().getDataFolder(), "examples");
        examplesFolder = new File(examplesFolder, exampleKey);
        if (!examplesFolder.exists()) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.example.unfetch.fail").replace("$example", exampleKey));
            return;
        }
        File backupFolder = new File(examplesFolder.getPath() + ".bak");
        if (backupFolder.exists()) {
            ConfigurationUtils.deleteDirectory(backupFolder);
            examplesFolder.renameTo(backupFolder);
        }
        examplesFolder.renameTo(backupFolder);
        sender.sendMessage(magic.getMessages().get("commands.mconfig.example.unfetch.success")
            .replace("$example", exampleKey)
            .replace("$backup", backupFolder.getName()));
    }

    protected void onSetExample(CommandSender sender, String[] parameters) {
        Set<String> loadedExamples = new HashSet<>(controller.getLoadedExamples());
        if (parameters.length == 0 || parameters[0].equals("none")) {
            if (configureExamples(sender, loadedExamples, null)) {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.example.set.clear"));
            }
            return;
        }
        String example = parameters[0];
        Set<String> examples;
        boolean setExamples = false;
        if (parameters.length == 1) {
            examples = new LinkedHashSet<>(controller.getLoadedExamples());
            examples.remove(controller.getExample());
        } else {
            setExamples = true;
            examples = new LinkedHashSet<>();
            if (parameters.length > 2 || !parameters[1].equalsIgnoreCase("none")) {
                for (int i = 1; i < parameters.length; i++) {
                    examples.add(parameters[i]);
                }
            }
        }
        if (configureExamples(sender, examples, example)) {
            if (setExamples) {
                if (examples.isEmpty()) {
                    sender.sendMessage(magic.getMessages().get("commands.mconfig.example.set.clear_added")
                        .replace("$example", example));
                } else {
                    sender.sendMessage(magic.getMessages().get("commands.mconfig.example.set.multiple")
                        .replace("$examples", StringUtils.join(examples, ","))
                        .replace("$example", example));
                }
            } else {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.example.set.success").replace("$example", example));
            }
        }
    }

    protected boolean configureExamples(CommandSender sender, Set<String> examples, String example) {
        return configureExamples(sender, examples, example, true);
    }

    protected boolean configureExamples(CommandSender sender, Set<String> examples, String example, boolean reload) {
        if (example == null) {
            example = "";
        }
        examples.remove(example);
        File exampleFile = new File(magic.getPlugin().getDataFolder() + File.separator + "config", EXAMPLES_FILE_NAME);
        try {
            YamlConfiguration exampleConfig = new YamlConfiguration();
            if (exampleFile.exists()) {
                exampleConfig.load(exampleFile);
            }
            exampleConfig.set("example", example);
            exampleConfig.set("examples", new ArrayList<>(examples));
            exampleConfig.set("add_examples", new ArrayList<>());
            exampleConfig.save(exampleFile);
            if (reload) {
                magic.loadConfigurationExamples(sender);
            }
        } catch (Exception ex) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.write_failed").replace("$file", exampleFile.getName()));
            magic.getLogger().log(Level.SEVERE, "Could not write to file " + exampleFile.getAbsoluteFile(), ex);
            return false;
        }

        return true;
    }

    protected void onReset(CommandSender sender, String[] parameters) {
        String fileType = parameters.length == 0 ? null : getFileParameter(parameters[0]);
        if (fileType == null) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig.reset.usage"), "", "", '|'));
            return;
        }

        if ((fileType.equals("config") || fileType.equals("messages")) && parameters.length == 1) {
            File pluginFolder = api.getPlugin().getDataFolder();
            File configFile = new File(pluginFolder, fileType + ".yml");
            boolean resetAny = false;
            if (configFile.exists()) {
                // This file should always exist unless it was just deleted without reloading.
                // So let's see if it's actually been edited
                boolean isEmpty = false;
                try {
                    YamlConfiguration configuration = new YamlConfiguration();
                    configuration.load(configFile);
                    isEmpty = configuration.getKeys(false).isEmpty();
                } catch (Exception ignore) {
                }
                if (!isEmpty) {
                    if (backupAndDelete(sender, configFile)) {
                        resetAny = true;
                    }
                }
            }
            configFile = new File(pluginFolder, fileType);
            configFile = new File(configFile, CUSTOM_FILE_NAME);
            if (configFile.exists()) {
                if (backupAndDelete(sender, configFile)) {
                    resetAny = true;
                }
            }
            if (fileType.equals("config")) {
                configFile = new File(pluginFolder, fileType);
                configFile = new File(configFile, EXAMPLES_FILE_NAME);
                if (configFile.exists()) {
                    if (backupAndDelete(sender, configFile)) {
                        resetAny = true;
                    }
                }
            }
            if (resetAny) {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.reset.load_prompt"));
            } else {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.reset.none"));
            }
            return;
        }

        if (parameters.length < 2) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig.reset.usage"), "", "", '|'));
            return;
        }
        String key = parameters[1];
        File pluginFolder = api.getPlugin().getDataFolder();
        File customFolder = new File(pluginFolder, fileType);
        File customFile = new File(customFolder, key + ".yml");
        boolean deleted = false;
        boolean removed = false;
        if (customFile.exists()) {
            backupAndDelete(sender, customFile);
            deleted = true;
        }
        File configFile = getConfigFile(sender, "reset", parameters);
        if (configFile != null && configFile.exists()) {
            YamlConfiguration customizations = new YamlConfiguration();
            try {
                customizations.load(configFile);
                if (customizations.contains(key)) {
                    customizations.set(key, null);
                    customizations.save(configFile);
                    sender.sendMessage(magic.getMessages().get("commands.mconfig.reset.removed").replace("$key", key));
                    removed = true;
                }
            } catch (Exception ex) {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.write_failed").replace("$file", configFile.getName()));
                magic.getLogger().log(Level.SEVERE, "Could not write to file " + configFile.getAbsoluteFile(), ex);
            }
        }
        if (deleted || removed) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.reset.load_prompt"));
        } else {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.reset.nothing").replace("$file", customFile.getName()).replace("$key", key));
        }
    }

    protected boolean backupAndDelete(CommandSender sender, File configFile) {
        File backupFile = new File(configFile.getAbsolutePath() + ".bak");
        boolean success = false;
        try {
            success = configFile.renameTo(backupFile);
        } catch (Exception ex) {
            success = false;
            magic.getLogger().log(Level.SEVERE, "Could not write to file " + backupFile.getAbsoluteFile(), ex);
        }
        if (!success) {
            sender.sendMessage(magic.getMessages().get("commands.mconfig.write_failed").replace("$file", backupFile.getName()));
            return false;
        }
        sender.sendMessage(magic.getMessages().get("commands.mconfig.reset.success").replace("$file", configFile.getName()));
        sender.sendMessage(magic.getMessages().get("commands.mconfig.reset.backup").replace("$backup", backupFile.getName()));
        return success;
    }

    protected void onMagicLanguage(CommandSender sender, String[] parameters) {
        String language = "";
        if (parameters.length > 0 && !parameters[0].equalsIgnoreCase("EN")) {
            language = parameters[0].toUpperCase();
        }

        String fileType = "config";
        File configFile = getConfigFile(fileType);
        String path = "language";
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        setPath(configuration, path, ConfigurationUtils.convertProperty(language));
        trySave("configure", sender, configFile, configuration, fileType, path);
    }

    protected void onMagicConfigure(CommandSender sender, String[] parameters) {
        File configFile = getConfigFile(sender, "configure", parameters);
        if (configFile == null) {
            return;
        }
        String fileType = parameters.length == 0 ? null : getFileParameter(parameters[0]);
        if (parameters.length < 3 || fileType == null) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig.configure.usage"), "", "", '|'));
            return;
        }
        if (fileType.equals("config") || fileType.equals("messages")) {
            String path = parameters[1];
            String value = "";
            if (parameters.length > 2) {
                value = StringUtils.join(Arrays.copyOfRange(parameters, 2, parameters.length), ' ');
            }
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
            setPath(configuration, path, ConfigurationUtils.convertProperty(value));
            trySave("configure", sender, configFile, configuration, fileType, path);
            return;
        }
        String key = parameters[1];
        String path = key + "." + parameters[2];
        String value = "";
        if (parameters.length > 3) {
            value = StringUtils.join(Arrays.copyOfRange(parameters, 3, parameters.length), ' ');
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        setPath(configuration, path, ConfigurationUtils.convertProperty(value));
        trySave("configure", sender, configFile, configuration, fileType, key);
    }

    protected void onMagicClean(CommandSender sender, String configName) {
        sender.sendMessage("This command is (temporarily?) disabled until it can be fixed.");
        return;
        /*
        List<String> configFiles = new ArrayList<>();
        if (!configName.isEmpty()) {
            configFiles.add(configName);
        } else {
            configFiles.add("spells");
            configFiles.add("wands");
            configFiles.add("paths");
            configFiles.add("mobs");
            configFiles.add("items");
            configFiles.add("crafting");
            configFiles.add("materials");
            configFiles.add("messages");
        }

        File pluginFolder = api.getPlugin().getDataFolder();
        Collection<String> examples = controller.getLoadedExamples();
        Plugin plugin = controller.getPlugin();
        for (String configFileName : configFiles) {
            sender.sendMessage(ChatColor.AQUA + "Checking " + ChatColor.DARK_AQUA + configFileName);
            try {
                File defaultsFile = new File(pluginFolder, "defaults/" + configFileName + ".defaults.yml");
                File configFile = new File(pluginFolder, configFileName + ".yml");

                YamlConfiguration cleanConfig = new YamlConfiguration();
                YamlConfiguration currentConfig = new YamlConfiguration();
                currentConfig.load(configFile);

                YamlConfiguration defaultConfig = new YamlConfiguration();
                defaultConfig.load(defaultsFile);

                // Overlay examples
                for (String example : examples) {
                    String examplesFileName = "examples/" + example + "/" + configFileName + ".yml";
                    InputStream input = plugin.getResource(examplesFileName);
                    if (input != null)
                    {
                        ConfigurationSection exampleConfig = CompatibilityUtils.loadConfigurationQuietly(input);
                        ConfigurationUtils.addConfigurations(defaultConfig, exampleConfig, false);
                    }
                }

                Collection<String> allKeys = currentConfig.getKeys(true);
                for (String key : allKeys) {
                    Object defaultValue = defaultConfig.get(key);
                    Object configValue = currentConfig.get(key);

                    // Will be covered by children
                    if (configValue instanceof ConfigurationSection) continue;

                    if (areDifferent(configValue, defaultValue)) {
                        cleanConfig.set(key, configValue);
                    }
                }

                int originalTopSize = currentConfig.getKeys(false).size();
                int cleanTopSize = cleanConfig.getKeys(false).size();
                int cleanSize = cleanConfig.getKeys(true).size();
                int removedCount = allKeys.size() - cleanSize;
                if (removedCount > 0) {
                    int removedTopCount = originalTopSize - cleanTopSize;
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "Removed " + ChatColor.GOLD + removedTopCount
                            + ChatColor.LIGHT_PURPLE + " top-level sections and " + ChatColor.GOLD + removedCount
                            + ChatColor.LIGHT_PURPLE + " total sections."
                    );

                    File backupFile = new File(pluginFolder, configFileName + ".yml.bak");
                    if (backupFile.exists()) {
                        sender.sendMessage(ChatColor.YELLOW + "  Backup file exists, will not overwrite: " + backupFile.getName());
                    } else {
                        sender.sendMessage(ChatColor.DARK_PURPLE + "  Saved backup file to " + backupFile.getName() + ", delete this file if all looks good.");
                        Files.copy(configFile, backupFile);
                    }
                    String[] lines = StringUtils.split(cleanConfig.saveToString(), '\n');
                    PrintWriter out = new PrintWriter(configFile, "UTF-8");
                    out.println("#");
                    out.println("# Default options have been removed from the file via /mconfig clean");
                    out.println("#");
                    for (String line : lines) {
                        if (!line.startsWith(" ")) {
                            out.println("");
                        }
                        out.println(line);
                    }
                    out.close();;
                } else {
                    sender.sendMessage(ChatColor.GOLD + "Looks clean to me!");
                }

            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "An error occurred, please check logs!");
                ex.printStackTrace();
            }
        }
        */
    }
}
