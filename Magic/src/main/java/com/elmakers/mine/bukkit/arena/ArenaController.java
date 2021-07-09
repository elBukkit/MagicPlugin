package com.elmakers.mine.bukkit.arena;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.geysermc.connector.common.ChatColor;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ArenaController implements Runnable {
    private final Map<String, ArenaTemplate> templates = new HashMap<>();
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<Player, ArenaPlayer> arenaPlayers = new WeakHashMap<>();
    private final Map<Entity, Arena> arenaMobs = new WeakHashMap<>();
    private final Plugin plugin;
    private final MageController magic;
    private final Object saveLock = new Object();

    public ArenaController(MageController magic) {
        this.magic = magic;
        this.plugin = magic.getPlugin();
    }

    public void start() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimer(plugin, this, 1, 10);
    }

    public Arena addArena(String arenaName, String templateKey, Location location) {
        ArenaTemplate template = getTemplate(templateKey);
        Arena arena = new Arena(arenaName, template, this, location);
        arenas.put(arenaName, arena);
        return arena;
    }

    public ArenaTemplate getTemplate(String templateKey) {
        ArenaTemplate arenaTemplate = templates.get(templateKey);
        if (arenaTemplate == null) {
            arenaTemplate = new ArenaTemplate(templateKey);
            templates.put(templateKey, arenaTemplate);
        }
        return arenaTemplate;
    }

    public void saveData(ConfigurationSection configuration) {
        Collection<String> oldKeys = configuration.getKeys(false);
        for (String oldKey : oldKeys) {
            configuration.set(oldKey, null);
        }
        for (Arena arena : arenas.values()) {
            ConfigurationSection arenaConfig = configuration.createSection(arena.getKey());
            arena.save(arenaConfig);
        }
    }

    public void loadTemplates(ConfigurationSection configuration) {
        if (configuration == null) return;

        Collection<String> templateKeys = configuration.getKeys(false);
        templates.clear();
        for (String templateKey : templateKeys) {
            templates.put(templateKey, new ArenaTemplate(templateKey, configuration.getConfigurationSection(templateKey)));
        }
        for (Arena arena : arenas.values()) {
            arena.reload();
        }
    }

    public void loadArenas(ConfigurationSection configuration) {
        if (configuration == null) return;
        Collection<String> arenaKeys = configuration.getKeys(false);
        for (String arenaKey : arenaKeys) {
            ConfigurationSection arenaConfiguration = configuration.getConfigurationSection(arenaKey);
            String templateKey = configuration.getString("template", arenaKey);
            ArenaTemplate template = getTemplate(templateKey);
            Arena arena = new Arena(arenaKey, template, this);
            arena.load(arenaConfiguration);
            arenas.put(arenaKey, arena);
        }
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Arena getMobArena(Entity entity) {
        return arenaMobs.get(entity);
    }

    public Arena getArena(String arenaName) {
        return arenas.get(arenaName);
    }

    public Arena getArena(Player player) {
        ArenaPlayer arenaPlayer = getArenaPlayer(player);
        return arenaPlayer == null || !arenaPlayer.isBattling() ? null : arenaPlayer.getArena();
    }

    public Arena getQueuedArena(Player player) {
        ArenaPlayer arenaPlayer = getArenaPlayer(player);
        return arenaPlayer == null ? null : arenaPlayer.getArena();
    }

    public ArenaPlayer getArenaPlayer(Player player) {
        return arenaPlayers.get(player);
    }

    public void unregister(Player player) {
        arenaPlayers.remove(player);
    }

    public void unregister(Entity entity) {
        arenaMobs.remove(entity);
    }

    public void register(Player player, ArenaPlayer arenaPlayer) {
        arenaPlayers.put(player, arenaPlayer);
    }

    public void register(Entity entity, Arena arena) {
        arenaMobs.put(entity, arena);
    }

    protected File getDataFile(String fileName) {
        return new File(plugin.getDataFolder(), fileName + ".yml");
    }

    public void remove(String arenaName) {
        Arena arena = arenas.remove(arenaName);
        if (arena != null) {
            arena.delete();
        }
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public Collection<String> getArenaKeys() {
        return arenas.keySet();
    }

    public Collection<String> getArenaTemplateKeys() {
        return templates.keySet();
    }

    public ArenaPlayer leave(Player player) {
        ArenaPlayer arenaPlayer = getArenaPlayer(player);
        if (arenaPlayer != null) {
            Arena arena = arenaPlayer.getArena();
            arena.remove(player);
            player.sendMessage("You have left " + arena.getName());
            arenaPlayer.teleport(arena.getExit());
            arena.check();
        }

        return arenaPlayer;
    }

    public void reset(Player player) {
        for (Arena arena : arenas.values()) {
            arena.reset(player);
        }
    }

    public void reset() {
        for (Arena arena : arenas.values()) {
            arena.reset();
        }
    }

    public MageController getMagic() {
        return magic;
    }

    @Override
    public void run() {
        for (Arena arena : arenas.values()) {
            if (arena.isStarted()) {
                arena.tick();
            }
        }
    }

    public void cancel() {
        for (Arena arena : arenas.values()) {
            if (arena.isStarted()) {
                arena.stop();
            }
        }
    }

    public void importArenas(CommandSender sender) {
        File pluginFolder = plugin.getDataFolder().getParentFile();
        File magicArenasFolder = new File(pluginFolder, "MagicArenas");
        if (!magicArenasFolder.exists()) {
            sender.sendMessage(ChatColor.RED + "Could not find MagicArenas folder, did you have it installed?");
            return;
        }

        YamlConfiguration saveConfiguration = new YamlConfiguration();
        YamlConfiguration arenaConfiguration = new YamlConfiguration();
        File arenaFile = new File(magicArenasFolder, "arenas.yml");
        if (arenaFile.exists()) {
            try {
                arenaConfiguration.load(arenaFile);
            } catch (IOException | InvalidConfigurationException ex) {
                sender.sendMessage(ChatColor.RED + "Could not read arena config file, please see console for errors");
                plugin.getLogger().log(Level.SEVERE, "Error loading " + arenaFile.getAbsolutePath(), ex);
            }
        }

        File saveFile = new File(magicArenasFolder, "data.yml");
        if (saveFile.exists()) {
            try {
                saveConfiguration.load(saveFile);
            } catch (IOException | InvalidConfigurationException ex) {
                sender.sendMessage(ChatColor.RED + "Could not read arena config file, please see console for errors");
                plugin.getLogger().log(Level.SEVERE, "Error loading " + arenaFile.getAbsolutePath(), ex);
            }
        }

        Collection<String> arenaKeys = arenaConfiguration.getKeys(false);
        for (String arenaKey : arenaKeys) {
            if (arenas.containsKey(arenaKey) || templates.containsKey(arenaKey)) {
                sender.sendMessage(ChatColor.YELLOW + "Skipping " + arenaKey + ", that arena already exists");
                continue;
            }

            ConfigurationSection templateConfiguration = arenaConfiguration.getConfigurationSection(arenaKey);
            ConfigurationSection saveData = saveConfiguration.getConfigurationSection(arenaKey);
            if (saveData == null) {
                saveData = ConfigurationUtils.newConfigurationSection();
            }

            // Migrate properties
            templateConfiguration.set("min_players", templateConfiguration.get("minplayers"));
            templateConfiguration.set("minplayers", null);
            templateConfiguration.set("max_players", templateConfiguration.get("maxplayers"));
            templateConfiguration.set("maxplayers", null);
            saveData.set("location", templateConfiguration.get("center"));

            ArenaTemplate template = new ArenaTemplate(arenaKey, templateConfiguration);
            templates.put(arenaKey, template);
            Arena arena = new Arena(arenaKey, template, this);
            arena.load(saveData);
            arenas.put(arenaKey, arena);
            arena.saveTemplate();
            sender.sendMessage(ChatColor.AQUA + "Imported " + arenaKey);
        }
        magic.getAPI().save();
    }
}
