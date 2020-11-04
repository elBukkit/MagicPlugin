package com.elmakers.mine.bukkit.magic.command;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.WandTemplate;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.command.config.NewSessionRequest;
import com.elmakers.mine.bukkit.magic.command.config.NewSessionRunnable;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

public class MagicConfigCommandExecutor extends MagicTabExecutor {
    private static final String CUSTOM_FILE_NAME = "_customizations.yml";
    private static Set<String> availableFiles = ImmutableSet.of(
            "spells", "wands", "automata", "classes", "config", "crafting", "effects",
            "items", "materials", "mobs", "paths", "attributes");
    private static final Map<String, String> availableFileMap = ImmutableMap.<String, String>builder()
        .put("spell", "spells")
        .put("wand", "wands")
        .put("automaton", "automata")
        .put("class", "classes")
        .put("recipe", "crafting")
        .put("effect", "effects")
        .put("item", "items")
        .put("material", "materials")
        .put("mob", "mobs")
        .put("path", "paths")
        .put("attribute", "attributes")
        .build();

    private final MagicController magic;
    private Gson gson;

    public MagicConfigCommandExecutor(MagicAPI api, MagicController controller) {
        super(api, "mconfig");
        this.magic = controller;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("Magic.commands.mconfig")) return options;

        if (args.length == 1) {
            addIfPermissible(sender, options, "Magic.commands.mconfig.", "clean");
            addIfPermissible(sender, options, "Magic.commands.mconfig.", "disable");
            addIfPermissible(sender, options, "Magic.commands.mconfig.", "enable");
            addIfPermissible(sender, options, "Magic.commands.mconfig.", "configure");
            addIfPermissible(sender, options, "Magic.commands.mconfig.", "editor");
            addIfPermissible(sender, options, "Magic.commands.mconfig.", "load");
            addIfPermissible(sender, options, "Magic.commands.mconfig.", "apply");
        }
        String subCommand = args[0];
        if (args.length == 2 && (subCommand.equals("disable") || subCommand.equals("enable") || subCommand.equals("configure") || subCommand.equals("editor"))) {
            options.addAll(availableFileMap.keySet());
        }
        if (args.length == 3 && (subCommand.equals("disable") ||  subCommand.equals("configure") ||  subCommand.equals("editor"))) {
            String fileType = getFileParameter(args[1]);
            if (fileType != null) {
                if (fileType.equals("spells")) {
                    Collection<SpellTemplate> spellList = api.getController().getSpellTemplates(true);
                    for (SpellTemplate spell : spellList) {
                        options.add(spell.getKey());
                    }
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
                if (fileType.equals("crafting")) {
                    Collection<String> recipeList = api.getController().getRecipeKeys();
                    for (String recipe : recipeList) {
                        options.add(recipe);
                    }
                }
                if (fileType.equals("mobs")) {
                    Collection<String> mobList = api.getController().getMobKeys();
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
                if (fileType.equals("automata")) {
                    Collection<String> list = api.getController().getAutomatonTemplateKeys();
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
            }
        }
        return options;
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
        if (!api.hasPermission(sender, "Magic.commands.mconfig." + subCommand)) {
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
        if (subCommand.equals("editor")) {
            onStartEditor(sender, parameters);
            return true;
        }
        if (subCommand.equals("load")) {
            sender.sendMessage("Not yet implemented, sorry!");
            return true;
        }
        if (subCommand.equals("apply")) {
            sender.sendMessage("Not yet implemented, sorry!");
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
        return message.replace("$type", type)
                .replace("$key", key)
                .replace("$options", StringUtils.join(availableFileMap.keySet(), delimiter));
    }

    @Nullable
    protected File getConfigFile(CommandSender sender, String command, String[] parameters) {
        if (parameters.length < 2) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig." + command + ".usage"), "", "", '|'));
            return null;
        }
        String fileKey = getFileParameter(parameters[0]);
        if (fileKey == null) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig." + command + ".nokey"), fileKey, "", ','));
            return null;
        }
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
        String editorType = "spell";
        if (parameters.length > 0) {
            editorType = parameters[0];
        }
        editorType = getFileParameter(editorType);
        if (editorType == null) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig.editor.usage"), "", "", '|'));
            return;
        }

        // Build session request
        NewSessionRequest newSession = new NewSessionRequest(editorType);
        if (sender instanceof Player) {
            newSession.setPlayer((Player)sender);
        }
        newSession.setLegacyIcons(magic.isLegacyIconsEnabled());
        newSession.setMagicVersion(getMagicVersion());
        newSession.setMinecraftVersion(CompatibilityUtils.getServerVersion());

        if (parameters.length > 1) {
            File pluginFolder = api.getPlugin().getDataFolder();
            String targetItem = parameters[1];
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

            ConfigurationSection targetConfig = defaultConfig.getConfigurationSection(targetItem);
            if (targetConfig == null) {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.new_item")
                    .replace("$type", editorType)
                    .replace("$item", targetItem));
                newSession.setName(targetItem);
            } else {
                sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.edit_item")
                    .replace("$type", editorType)
                    .replace("$item", targetItem));
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.set(targetItem, targetConfig);
                newSession.setContents(yaml.saveToString());
            }
        }

        // Send request
        sender.sendMessage(magic.getMessages().get("commands.mconfig.editor.wait"));
        final Plugin plugin = magic.getPlugin();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new NewSessionRunnable(magic, getGson(), sender, newSession));
    }

    protected void onMagicConfigure(CommandSender sender, String[] parameters) {
        File configFile = getConfigFile(sender, "configure", parameters);
        if (configFile == null) {
            return;
        }
        if (parameters.length < 3) {
            sender.sendMessage(escapeMessage(magic.getMessages().get("commands.mconfig.configure.usage"), "", "", '|'));
            return;
        }
        String key = parameters[1];
        String path = key + "." + parameters[2];
        String value = "";
        if (parameters.length > 3) {
            value = StringUtils.join(Arrays.copyOfRange(parameters, 3, parameters.length), ' ');
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        setPath(configuration, path, value);
        trySave("configure", sender, configFile, configuration, parameters[0], key);
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
                        ConfigurationSection exampleConfig = CompatibilityUtils.loadConfiguration(input);
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

    @SuppressWarnings("unchecked")
    private boolean areDifferent(Object configValue, Object defaultValue) {
        if (defaultValue == null) return true;

        if (configValue instanceof ConfigurationSection || configValue instanceof Map) {
            if (!(defaultValue instanceof ConfigurationSection) && !(defaultValue instanceof Map)) return true;

            Map<String, Object> configMap = configValue instanceof ConfigurationSection
                    ? NMSUtils.getMap((ConfigurationSection)configValue)
                    : (Map<String, Object>)configValue;

            Map<String, Object> defaultMap = defaultValue instanceof ConfigurationSection
                    ? NMSUtils.getMap((ConfigurationSection)defaultValue)
                    : (Map<String, Object>)defaultValue;

            return !configMap.equals(defaultMap);
        }
        if (configValue instanceof List) {
             if (!(defaultValue instanceof List)) return true;

            List<Object> configList = (List<Object>)configValue;
            List<Object> defaultList = (List<Object>)defaultValue;
            if (configList.size() != defaultList.size()) return true;

            for (int i = 0; i < configList.size(); i++) {
                if (areDifferent(configList.get(i), defaultList.get(i))) return true;
            }

            return false;
        }

        return !defaultValue.equals(configValue);
    }
}
