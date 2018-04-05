package com.elmakers.mine.bukkit.magic.command;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.google.common.io.Files;

public class MagicConfigCommandExecutor extends MagicTabExecutor {

    public MagicConfigCommandExecutor(MagicAPI api, MagicController controller) {
        super(api);
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("Magic.commands.mconfig")) return options;

        if (args.length == 1) {
            addIfPermissible(sender, options, "Magic.commands.mconfig.clean", "clean");
        }
        return options;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!api.hasPermission(sender, "Magic.commands.mconfig"))
        {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0)
        {
            sender.sendMessage("Usage: mconfig [clean | update]");
            return true;
        }

        String subCommand = args[0];
        if (!api.hasPermission(sender, "Magic.commands.mconfig." + subCommand)) {
            sendNoPermission(sender);
            return true;
        }

        if (subCommand.equals("clean")) {
            onMagicClean(sender, args.length > 1 ? args[1] : "");
        } else if (subCommand.equals("clean")) {
            onMagicUpdate(sender, args.length > 1 ? args[1] : "");
        } else {
            sender.sendMessage("Usage: mconfig [clean | update]");
        }
        return true;
    }

    protected void onMagicUpdate(CommandSender sender, String configName) {
        // Not allowing this to run on all
        if (configName.isEmpty()) {
            sender.sendMessage("Usage: mconfig update [materials]");
            return;
        }

        File pluginFolder = api.getPlugin().getDataFolder();
        Collection<File> filesToCheck = new ArrayList<>();
        filesToCheck.add(new File(pluginFolder, configName + ".yml"));

        File subfolder = new File(pluginFolder, configName);
        File[] subconfigs = subfolder.listFiles();
        for (File file : subconfigs) {
            if (file.getName().endsWith(".yml")) {
                filesToCheck.add(file);
            }
        }

        for (File configFile : filesToCheck) {
            sender.sendMessage(ChatColor.AQUA + "Checking " + ChatColor.DARK_AQUA + configFile.getName());
            try {
                File backupFile = new File(configFile.getParentFile(), configFile.getName() + ".bak");
                if (backupFile.exists()) {
                    sender.sendMessage(ChatColor.RED + "Backup file already exists, please delete: " + ChatColor.WHITE + backupFile.getName());
                    continue;
                }

                YamlConfiguration configuration = new YamlConfiguration();
                configuration.load(configFile);

                int modified = 0;
                Set<String> keys = configuration.getKeys(false);
                for (String key : keys) {
                    List<String> stringList = ConfigurationUtils.getStringList(configuration, key);
                    if (stringList != null) {
                        List<String> newList = new ArrayList<>();
                        boolean updateList = false;
                        for (String materialKey : stringList) {
                            String migrated = DeprecatedUtils.migrateMaterial(materialKey);
                            if (migrated == null || migrated.equalsIgnoreCase(materialKey)) {
                                newList.add(materialKey);
                            } else {
                                newList.add(migrated);
                                updateList = true;
                                modified++;
                            }
                        }

                        if (updateList) {
                            configuration.set(key, newList);
                        }
                    }
                }


                if (modified > 0) {
                    sender.sendMessage(ChatColor.AQUA + "Updated " + ChatColor.WHITE + modified + ChatColor.AQUA + " materials and " + ChatColor.DARK_PURPLE
                        + "  Saved backup file to " + ChatColor.LIGHT_PURPLE + backupFile.getName() + ChatColor.DARK_PURPLE + ", delete this file if all looks good.");

                    Files.copy(configFile, backupFile);
                    configuration.save(configFile);
                }


            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "An error occurred updating " + ChatColor.WHITE + configFile.getName());
                controller.getLogger().log(Level.WARNING, "An error occurred updating " + configFile.getName(), ex);
            }
        }

    }

    protected void onMagicClean(CommandSender sender, String configName) {
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
