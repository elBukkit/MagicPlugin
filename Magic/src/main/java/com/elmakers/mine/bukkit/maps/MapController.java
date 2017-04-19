package com.elmakers.mine.bukkit.maps;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MapController implements com.elmakers.mine.bukkit.api.maps.MapController {
    private final File configurationFile;
    private final File cacheFolder;
    private final Plugin plugin;

    private boolean loaded = false;
    private boolean disabled = false;
    private BukkitTask saveTask = null;

    private HashMap<String, URLMap> keyMap = new HashMap<>();
    private HashMap<Short, URLMap> idMap = new HashMap<>();

    public MapController(Plugin plugin, File configFile, File cache) {
        this.plugin = plugin;
        this.configurationFile = configFile;
        this.cacheFolder = cache;
    }

    // Public API
    @Override
    public List<com.elmakers.mine.bukkit.api.maps.URLMap> getAll() {
        return new ArrayList<com.elmakers.mine.bukkit.api.maps.URLMap>(idMap.values());
    }

    public void loadConfiguration() {
        if (configurationFile == null) return;

        YamlConfiguration configuration = new YamlConfiguration();
        if (configurationFile.exists()) {
            try {
                info("Loading image map data from " + configurationFile.getName());
                configuration.load(configurationFile);
                Set<String> maps = configuration.getKeys(false);
                boolean needsUpdate = false;
                for (String mapIdString : maps) {
                    ConfigurationSection mapConfig = configuration.getConfigurationSection(mapIdString);
                    try {
                        Short mapId = null;
                        URLMap map = null;
                        Integer priority = null;
                        if (mapConfig.contains("priority")) {
                            priority = mapConfig.getInt("priority");
                        }
                        Integer xOverlay = null;
                        if (mapConfig.contains("x_overlay")) {
                            xOverlay = mapConfig.getInt("x_overlay");
                        }
                        Integer yOverlay = null;
                        if (mapConfig.contains("y_overlay")) {
                            yOverlay = mapConfig.getInt("y_overlay");
                        }
                        String world = "world";
                        if (mapConfig.contains("world")) {
                            world = mapConfig.getString("world");
                        }
                        try {
                            mapId = Short.parseShort(mapIdString);
                        } catch (Exception ex) {
                            map = get(world, mapConfig.getString("url"), mapConfig.getString("name"),
                                    mapConfig.getInt("x"), mapConfig.getInt("y"), xOverlay, yOverlay,
                                    mapConfig.getInt("width"), mapConfig.getInt("height"), priority);
                            info("Created new map id " + map.id + " for config id " + mapIdString);
                            needsUpdate = true;
                        }
                        if (map == null && mapId != null) {
                            map = get(world, mapId, mapConfig.getString("url"), mapConfig.getString("name"),
                                    mapConfig.getInt("x"), mapConfig.getInt("y"),
                                    xOverlay, yOverlay, mapConfig.getInt("width"), mapConfig.getInt("height"), priority);
                        }

                        if (map == null) {
                            throw new Exception("Failed to load map id " + mapIdString);
                        }

                        // Check for disabled maps
                        if (!mapConfig.getBoolean("enabled")) {
                            map.disable();
                        } else {
                            map.getMapView();
                        }
                    } catch (Exception ex) {
                        warning("Failed to load " + configurationFile.getAbsolutePath() +
                                ": " + ex.getMessage() + ", saving will be disabled until this issues is resolved");
                        disabled = true;
                    }
                }

                if (needsUpdate) {
                    save();
                }

                info("Loaded " + keyMap.size() + " image maps");
            } catch (Exception ex) {
                warning("Failed to load " + configurationFile.getAbsolutePath() + ": " + ex.getMessage());
            }
        }
        loaded = true;
    }

    @Override
    public void save() {
        save(true);
    }

    private class SaveRunnable implements Runnable {
        final List<URLMap> saveMaps;

        public SaveRunnable(Collection<URLMap> maps) {
            saveMaps = new ArrayList<>(maps);
        }

        @Override
        public void run() {
            try {
                YamlConfiguration configuration = new YamlConfiguration();
                for (URLMap map : saveMaps) {
                    ConfigurationSection mapConfig = configuration.createSection(Short.toString(map.id));
                    mapConfig.set("world", map.world);
                    mapConfig.set("url", map.url);
                    mapConfig.set("x", map.x);
                    mapConfig.set("y", map.y);
                    mapConfig.set("width", map.width);
                    mapConfig.set("height", map.height);
                    mapConfig.set("enabled", map.isEnabled());
                    mapConfig.set("name", map.name);
                    if (map.priority != null) {
                        mapConfig.set("priority", map.priority);
                    }
                    if (map.xOverlay != null) {
                        mapConfig.set("x_overlay", map.xOverlay);
                    }
                    if (map.yOverlay != null) {
                        mapConfig.set("y_overlay", map.yOverlay);
                    }
                }
                File tempFile = new File(configurationFile.getAbsolutePath() + ".tmp");
                configuration.save(tempFile);
                if (configurationFile.exists()) {
                    File backupFile = new File(configurationFile.getAbsolutePath() + ".bak");
                    if (!backupFile.exists() || configurationFile.length() >= backupFile.length()) {
                        configurationFile.renameTo(backupFile);
                    }
                }
                tempFile.renameTo(configurationFile);
            } catch (Exception ex) {
                warning("Failed to save file " + configurationFile.getAbsolutePath());
            }
            saveTask = null;
        };
    }

    /**
     * Saves the configuration file.
     *
     * This is called automatically as changes are made, but you can call it in onDisable to be safe.
     */
    public void save(boolean asynchronous) {
        if (!loaded) {
            if (plugin == null) {
                Bukkit.getLogger().warning("[Magic] Attempted to save image map data before initialization");
            } else {
                plugin.getLogger().warning("Attempted to save image map data before initialization");
            }
            return;
        }
        if (configurationFile == null || disabled) return;
        if (asynchronous && (saveTask != null || plugin == null)) return;

        Runnable runnable = new SaveRunnable(idMap.values());
        if (asynchronous) {
            saveTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        } else {
            runnable.run();
        }
    }

    protected void info(String message)
    {
        if (plugin != null){
            plugin.getLogger().info(message);
        }
    }

    protected void warning(String message)
    {
        if (plugin != null){
            plugin.getLogger().warning(message);
        }
    }

    /**
     * Resets all internal data.
     *
     * Can be called prior to save() to permanently delete all map images.
     * Can also be called prior to load() to load a fresh config file.
     */
    public void resetAll() {
        for (URLMap map : keyMap.values()) {
            map.reset();
        }
    }

    public void clearCache() {
        String[] cacheFiles = cacheFolder.list();
        for (String cacheFilename : cacheFiles) {
            if (!cacheFilename.endsWith(".png") && !cacheFilename.endsWith(".jpeg") && !cacheFilename.endsWith(".jpg")
                    && !cacheFilename.endsWith(".gif")) continue;

            File cacheFile = new File(cacheFolder, cacheFilename);
            cacheFile.delete();
            if (plugin != null) {
                plugin.getLogger().info("Deleted file " + cacheFile.getAbsolutePath());
            }
        }
        loadConfiguration();
    }

    /**
     * A helper function to get an ItemStack from a MapView.
     *
     * @param name
     * 	The display name to give the new item. Optional.
     * @param mapId
     * @return
     */
    public ItemStack getMapItem(String name, short mapId) {
        ItemStack newMapItem = new ItemStack(Material.MAP, 1, mapId);
        if (name != null) {
            ItemMeta meta = newMapItem.getItemMeta();
            meta.setDisplayName(name);
            newMapItem.setItemMeta(meta);
        }
        return newMapItem;
    }

    /**
     * Get an ItemStack that is a headshot of a player's skin.
     *
     * @param worldName
     * @param playerName
     * @param priority
     * @param photoLabel
     * @return
     */
    @Override
    public ItemStack getPlayerPortrait(String worldName, String playerName, Integer priority, String photoLabel) {
        photoLabel = photoLabel == null ? playerName : photoLabel;
        MapView mapView = getURL(worldName, "http://skins.minecraft.net/MinecraftSkins/" + playerName + ".png", photoLabel, 8, 8, 40, 8, 8, 8, priority);
        return getMapItem(photoLabel, mapView);
    }

    public MapView getURL(String worldName, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
        URLMap map = get(worldName, url, name, x, y, xOverlay, yOverlay, width, height, priority);
        return map.getMapView();
    }

    @Override
    public ItemStack getMapItem(short id)
    {
        ItemStack newMapItem = new ItemStack(Material.MAP, 1, id);
        URLMap loadedMap = idMap.get(id);
        if (loadedMap != null)
        {
            String mapName = loadedMap.getName();
            if (mapName != null)
            {
                ItemMeta meta = newMapItem.getItemMeta();
                meta.setDisplayName(mapName);
                newMapItem.setItemMeta(meta);
            }
        }

        return newMapItem;
    }

    /**
     * Get a new ItemStack for the specified url with a specific cropping.
     *
     */
    @Override
    public ItemStack getURLItem(String world, String url, String name, int x, int y, int width, int height, Integer priority) {
        MapView mapView = getURL(world, url, name, x, y, null, null, width, height, priority);
        return getMapItem(name, mapView);
    }

    @Override
    @SuppressWarnings("deprecation")
    public short getURLMapId(String world, String url, String name, int x, int y, int width, int height, Integer priority) {
        MapView mapView = getURL(world, url, name, x, y, null, null, width, height, priority);
        return mapView == null ? 0 : mapView.getId();
    }

    @Override
    public short getURLMapId(String world, String url) {
        return getURLMapId(world, url, null, 0, 0, 0, 0, null);
    }

    @SuppressWarnings("deprecation")
    protected ItemStack getMapItem(String name, MapView mapView) {
        short id = 0;
        if (mapView != null) {
            id = mapView.getId();
        }
        return getMapItem(name, id);
    }

    /**
     * Force reload of a player headshot.
     *
     * @param worldName
     * @param playerName
     */
    @Override
    public void forceReloadPlayerPortrait(String worldName, String playerName) {
        forceReload(worldName, "http://s3.amazonaws.com/MinecraftSkins/" + playerName + ".png", 8, 8, 8, 8);
    }

    /**
     * Force reload of the specific url and cropping.
     *
     * @param worldName
     * @param url
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void forceReload(String worldName, String url, int x, int y, int width, int height) {
        get(worldName, url, x, y, width, height).reload();
    }

    /**
     * Force resending all maps to a specific player.
     * @param playerName
     */
    public void resend(String playerName) {
        for (URLMap map : keyMap.values()) {
            map.resendTo(playerName);
        }
    }

    @Override
    public void loadMap(String world, short id, String url, String name, int x, int y, int width, int height, Integer priority)
    {
        URLMap map = get(world, id, url, name, x, y, null, null, width, height, priority);
        map.getMapView();
    }

    @Override
    public boolean hasMap(short id)
    {
        return idMap.containsKey(id);
    }

    @Override
    public URLMap getMap(short id) {
        return idMap.get(id);
    }

    public URLMap get(String world, short mapId, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
        String key = URLMap.getKey(world, url, x, y, width, height);
        URLMap map = idMap.get(mapId);
        if (map != null) {
            if (!map.getKey().equals(key)) {
                warning("Two maps with the same id but different keys: " + mapId + ": " + key + ", " + map.getKey());
            }
            return map;
        }
        map = keyMap.get(key);
        if (map != null) {
            if (map.id != mapId) {
                warning("Two maps with the same key but different ids: " + key + ": " + mapId + ", " + map.id);
            }
            return map;
        }

        map = new URLMap(this, world, mapId, url, name, x, y, xOverlay, yOverlay, width, height, priority);
        keyMap.put(key, map);
        idMap.put(mapId, map);
        return map;
    }

    private URLMap get(String worldName, String url, int x, int y, int width, int height) {
        return get(worldName, url, x, y, width, height, null);
    }

    @SuppressWarnings("deprecation")
    private URLMap get(String worldName, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
        String key = URLMap.getKey(worldName, url, x, y, width, height);
        if (keyMap.containsKey(key)) {
            URLMap map = keyMap.get(key);
            map.priority = priority;
            map.name = name;
            map.xOverlay = xOverlay;
            map.yOverlay = yOverlay;
            return map;
        }
        World world = Bukkit.getWorld(worldName);
        MapView mapView = Bukkit.createMap(world);
        if (mapView == null) {
            warning("Unable to create new map for url key " + key);
            return null;
        }
        URLMap newMap = get(worldName, mapView.getId(), url, name, x, y, xOverlay, yOverlay, width, height, priority);
        save();
        return newMap;
    }

    private URLMap get(String worldName, String url, int x, int y, int width, int height, Integer priority) {
        return get(worldName, url, null, x, y, null, null, width, height, priority);
    }

    protected void remove(String key) {
        keyMap.remove(key);
    }

    protected Plugin getPlugin() {
        return plugin;
    }

    public File getCacheFolder() {
        return cacheFolder;
    }

    @Override
    public boolean remove(short id) {
        URLMap map = idMap.get(id);
        if (map != null) {
            map.enabled = false;
            keyMap.remove(map.getKey());
            idMap.remove(id);
            return true;
        }

        return false;
    }
}
