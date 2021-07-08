package com.elmakers.mine.bukkit.arena;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class ArenaController implements Runnable {
    private final Map<String, Arena> arenas = new HashMap<String, Arena>();
    private final Map<Player, ArenaPlayer> arenaPlayers = new WeakHashMap<>();
    private final Map<Entity, Arena> arenaMobs = new WeakHashMap<>();
    private final Plugin plugin;
    private final MageController magic;
    private final Object saveLock = new Object();

    private BukkitTask task = null;
    private String pathTemplate = "beginner";

    public ArenaController(MageController magic) {
        this.magic = magic;
        this.plugin = magic.getPlugin();
    }

    public Arena addArena(String arenaName, Location location, int min, int max, ArenaType type) {
        Arena arena = new Arena(arenaName, this, location, min, max, type);
        arenas.put(arenaName.toLowerCase(), arena);
        return arena;
    }

    public void save() {
        save(true);
    }

    public void save(boolean asynchronous) {
        final File arenaSaveFile = getDataFile("arenas");
        final YamlConfiguration arenaSaves = new YamlConfiguration();
        save(arenaSaves);

        try {
            arenaSaves.save(arenaSaveFile);
        } catch (Exception ex) {
            plugin.getLogger().warning("Error saving arena configuration to " + arenaSaveFile.getName());
            ex.printStackTrace();
        }

        if (asynchronous) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    synchronized (saveLock) {
                        try {
                            arenaSaves.save(arenaSaveFile);
                        } catch (Exception ex) {
                            plugin.getLogger().warning("Error saving arena configuration to " + arenaSaveFile.getName());
                            ex.printStackTrace();
                        }
                    }
                }
            });
        } else {
            try {
                arenaSaves.save(arenaSaveFile);
            } catch (Exception ex) {
                plugin.getLogger().warning("Error saving arena configuration to " + arenaSaveFile.getName() + " synchronously");
                ex.printStackTrace();
            }
        }
    }

    private void save(ConfigurationSection configuration) {
        Collection<String> oldKeys = configuration.getKeys(false);
        for (String oldKey : oldKeys) {
            configuration.set(oldKey, null);
        }
        for (Arena arena : arenas.values()) {
            ConfigurationSection arenaConfig = configuration.createSection(arena.getKey());
            arena.save(arenaConfig);
        }
    }

    private void saveData(ConfigurationSection configuration) {
        Collection<String> oldKeys = configuration.getKeys(false);
        for (String oldKey : oldKeys) {
            configuration.set(oldKey, null);
        }
        for (Arena arena : arenas.values()) {
            ConfigurationSection arenaConfig = configuration.createSection(arena.getKey());
            arena.saveData(arenaConfig);
        }
    }

    public void saveData() {
        saveData(true);
    }

    public void saveData(boolean asynchronous) {
        final File arenaSaveFile = getDataFile("data");
        final YamlConfiguration arenaSaves = new YamlConfiguration();
        saveData(arenaSaves);

        try {
            arenaSaves.save(arenaSaveFile);
        } catch (Exception ex) {
            plugin.getLogger().warning("Error saving arena data to " + arenaSaveFile.getName());
            ex.printStackTrace();
        }

        if (asynchronous) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    synchronized (saveLock) {
                        try {
                            arenaSaves.save(arenaSaveFile);
                        } catch (Exception ex) {
                            plugin.getLogger().warning("Error saving arena data to " + arenaSaveFile.getName());
                            ex.printStackTrace();
                        }
                    }
                }
            });
        } else {
            try {
                arenaSaves.save(arenaSaveFile);
            } catch (Exception ex) {
                plugin.getLogger().warning("Error saving arena data to " + arenaSaveFile.getName() + " synchronously");
                ex.printStackTrace();
            }
        }
    }

    public void load() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        if (task != null) {
            task.cancel();
            task = null;
        }
        ConfigurationSection arenaConfiguration = loadDataFile("arenas");
        load(arenaConfiguration);

        ConfigurationSection arenaData = loadDataFile("data");
        loadData(arenaData);

        plugin.reloadConfig();
        Configuration config = plugin.getConfig();
        pathTemplate = config.getString("path_template", pathTemplate);

        task = scheduler.runTaskTimer(plugin, this, 1, 10);
    }

    private void load(ConfigurationSection configuration) {
        if (configuration == null) return;

        Collection<String> arenaKeys = configuration.getKeys(false);

        for (Arena arena : arenas.values()) {
            arena.remove();
        }
        arenas.clear();
        for (String arenaKey : arenaKeys) {
            Arena arena = new Arena(arenaKey, this);
            arena.load(configuration.getConfigurationSection(arenaKey));
            arenas.put(arenaKey.toLowerCase(), arena);
        }

        plugin.getLogger().info("Loaded " + arenas.size() + " arenas");
    }

    private void loadData(ConfigurationSection configuration) {
        if (configuration == null) return;

        Collection<String> arenaKeys = configuration.getKeys(false);

        for (String arenaKey : arenaKeys) {
            Arena arena = arenas.get(arenaKey);
            if (arena != null) {
                arena.loadData(configuration.getConfigurationSection(arenaKey));
            }
        }
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Arena getMobArena(Entity entity) {
        return arenaMobs.get(entity);
    }

    public Arena getArena(String arenaName) {
        return arenas.get(arenaName.toLowerCase());
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

    protected ConfigurationSection loadDataFile(String fileName) {
        File dataFile = getDataFile(fileName);
        if (!dataFile.exists()) {
            return null;
        }
        Configuration configuration = YamlConfiguration.loadConfiguration(dataFile);
        return configuration;
    }

    protected File getDataFile(String fileName) {
        return new File(plugin.getDataFolder(), fileName + ".yml");
    }

    public void remove(String arenaName) {
        Arena arena = arenas.get(arenaName.toLowerCase());
        if (arena != null) {
            arena.remove();
            arenas.remove(arenaName.toLowerCase());
        }
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
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

    public String getPathTemplate() {
        return pathTemplate;
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
}
