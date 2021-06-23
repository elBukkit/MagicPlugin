package com.elmakers.mine.bukkit.maps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MapController implements com.elmakers.mine.bukkit.api.maps.MapController {
    protected final File configurationFile;
    private final File cacheFolder;
    private final Plugin plugin;

    private boolean animationAllowed = true;
    private boolean loaded = false;
    private boolean disabled = false;
    protected BukkitTask saveTask = null;

    private HashMap<String, URLMap> keyMap = new HashMap<>();
    private HashMap<String, URLMap> playerMap = new HashMap<>();
    private HashMap<Integer, URLMap> idMap = new HashMap<>();

    public MapController(Plugin plugin, File configFile, File cache) {
        this.plugin = plugin;
        this.configurationFile = configFile;
        this.cacheFolder = cache;
    }

    // Public API
    @Override
    public List<com.elmakers.mine.bukkit.api.maps.URLMap> getAll() {
        return new ArrayList<>(idMap.values());
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
                        Integer mapId = null;
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
                        String playerName = null;
                        if (mapConfig.contains("player")) {
                            playerName = mapConfig.getString("player");
                        }
                        try {
                            mapId = Integer.parseInt(mapIdString);
                        } catch (Exception ex) {
                            map = get(world, mapConfig.getString("url"), mapConfig.getString("name"),
                                    mapConfig.getInt("x"), mapConfig.getInt("y"), xOverlay, yOverlay,
                                    mapConfig.getInt("width"), mapConfig.getInt("height"), priority, playerName);
                            info("Created new map id " + map.id + " for config id " + mapIdString);
                            needsUpdate = true;
                        }
                        if (map == null && mapId != null) {
                            map = get(world, mapId, mapConfig.getString("url"), mapConfig.getString("name"),
                                    mapConfig.getInt("x"), mapConfig.getInt("y"),
                                    xOverlay, yOverlay, mapConfig.getInt("width"), mapConfig.getInt("height"), priority, playerName);
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
                        warning("Failed to load " + configurationFile.getAbsolutePath()
                                + ": " + ex.getMessage() + ", saving will be disabled until this issues is resolved");
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

    /**
     * Saves the configuration file.
     *
     * <p>This is called automatically as changes are made, but you can call it in onDisable to be safe.
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

        Runnable runnable = new SaveMapsRunnable(this, idMap.values());
        if (asynchronous) {
            saveTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        } else {
            runnable.run();
        }
    }

    protected void info(String message)
    {
        if (plugin != null) {
            plugin.getLogger().info(message);
        }
    }

    protected void warning(String message)
    {
        if (plugin != null) {
            plugin.getLogger().warning(message);
        }
    }

    /**
     * Resets all internal data.
     *
     * <p>Can be called prior to save() to permanently delete all map images.
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
     * Get an ItemStack that is a headshot of a player's skin.
     */
    @Nullable
    @Override
    public ItemStack getPlayerPortrait(String worldName, String playerName, Integer priority, String photoLabel) {
        photoLabel = photoLabel == null ? playerName : photoLabel;
        String url = CompatibilityLib.getSkinUtils().getOnlineSkinURL(playerName);
        if (url != null) {
            MapView mapView = getURL(worldName, url, photoLabel, 8, 8, 40, 8, 8, 8, priority, playerName);
            return getMapItem(photoLabel, mapView);
        }
        MapView mapView = getURL(worldName, null, photoLabel, 8, 8, 40, 8, 8, 8, priority, playerName);
        return getMapItem(photoLabel, mapView);
    }

    @Nullable
    public MapView getURL(String worldName, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
        URLMap map = get(worldName, url, name, x, y, xOverlay, yOverlay, width, height, priority);
        return map.getMapView();
    }

    @Nullable
    public MapView getURL(String worldName, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority, String playerName) {
        URLMap map = get(worldName, url, name, x, y, xOverlay, yOverlay, width, height, priority, playerName);
        return map.getMapView();
    }

    /**
     * A helper function to get an ItemStack from a MapView.
     *
     * @param name The display name to give the new item. Optional.
     * @return
     */
    public ItemStack getMapItem(String name, int mapId) {
        ItemStack newMapItem = createMap(mapId);
        if (name != null) {
            ItemMeta meta = newMapItem.getItemMeta();
            meta.setDisplayName(name);
            newMapItem.setItemMeta(meta);
        }
        return newMapItem;
    }

    @Override
    public ItemStack getMapItem(int id)
    {
        ItemStack newMapItem = createMap(id);
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

    @SuppressWarnings("deprecation")
    protected ItemStack getMapItem(String name, MapView mapView) {
        int id = 0;
        if (mapView != null) {
            id = mapView.getId();
        }
        return getMapItem(name, id);
    }

    // This is copied from MagicController, which I'm still trying to keep out of this class. Shrug?
    public ItemStack createMap(int mapId) {
        short durability = CompatibilityLib.isCurrentVersion() ? 0 : (short)mapId;
        ItemStack mapItem = CompatibilityLib.getDeprecatedUtils().createItemStack(DefaultMaterials.getFilledMap(), 1, durability);
        if (CompatibilityLib.isCurrentVersion()) {
            mapItem = CompatibilityLib.getItemUtils().makeReal(mapItem);
            CompatibilityLib.getNBTUtils().setMetaInt(mapItem, "map", mapId);
        }
        return mapItem;
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
    public int getURLMapId(String world, String url, String name, int x, int y, int width, int height, Integer priority) {
        MapView mapView = getURL(world, url, name, x, y, null, null, width, height, priority);
        return mapView == null ? 0 : mapView.getId();
    }

    @Override
    public int getURLMapId(String world, String url) {
        return getURLMapId(world, url, null, 0, 0, 0, 0, null);
    }

    /**
     * Force reload of a player headshot.
     */
    @Override
    public void forceReloadPlayerPortrait(String worldName, String playerName) {
        String url = CompatibilityLib.getSkinUtils().getOnlineSkinURL(playerName);
        if (url != null) {
            forceReload(worldName, url, 8, 8, 8, 8);
        }
    }

    /**
     * Force reload of the specific url and cropping.
     */
    public void forceReload(String worldName, String url, int x, int y, int width, int height) {
        get(worldName, url, x, y, width, height).reload();
    }

    /**
     * Force resending all maps to a specific player.
     */
    public void resend(String playerName) {
        for (URLMap map : keyMap.values()) {
            map.resendTo(playerName);
        }
    }

    @Override
    public void loadMap(String world, int id, String url, String name, int x, int y, int width, int height, Integer priority)
    {
        URLMap map = get(world, id, url, name, x, y, null, null, width, height, priority, null);
        map.getMapView();
    }

    @Override
    public boolean hasMap(int id)
    {
        return idMap.containsKey(id);
    }

    @Override
    public URLMap getMap(int id) {
        return idMap.get(id);
    }

    public URLMap get(String world, int mapId, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
        return get(world, mapId, url, name, x, y, xOverlay, yOverlay, width, height, priority, null);
    }

    public URLMap get(String world, int mapId, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority, String playerName) {
        String key = URLMap.getKey(world, url, playerName, x, y, width, height);
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

        map = new URLMap(this, world, mapId, url, name, x, y, xOverlay, yOverlay, width, height, priority, playerName);
        keyMap.put(key, map);
        idMap.put(mapId, map);
        if (playerName != null) {
            playerMap.put(playerName, map);
        }
        return map;
    }

    @Nullable
    private URLMap get(String worldName, String url, int x, int y, int width, int height) {
        return get(worldName, url, x, y, width, height, null);
    }

    @Nullable
    private URLMap get(String worldName, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
        return get(worldName, url, name, x, y, xOverlay, yOverlay, width, height, priority, null);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    private URLMap get(String worldName, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority, String playerName) {
        URLMap existing = null;
        if (url == null) {
            existing = playerMap.get(playerName);
        } else {
            String key = URLMap.getKey(worldName, url, playerName, x, y, width, height);
            existing = keyMap.get(key);
        }
        if (existing != null) {
            existing.priority = priority;
            existing.name = name;
            existing.xOverlay = xOverlay;
            existing.yOverlay = yOverlay;
            return existing;
        }
        World world = Bukkit.getWorld(worldName);
        MapView mapView = Bukkit.createMap(world);
        if (mapView == null) {
            if (url == null) {
                warning("Unable to create new map for player " + playerName);
            } else {
                warning("Unable to create new map for url " + url);
            }
            return null;
        }
        URLMap newMap = get(worldName, mapView.getId(), url, name, x, y, xOverlay, yOverlay, width, height, priority, playerName);
        save();
        return newMap;
    }

    @Nullable
    private URLMap get(String worldName, String url, int x, int y, int width, int height, Integer priority) {
        return get(worldName, url, null, x, y, null, null, width, height, priority);
    }

    protected Plugin getPlugin() {
        return plugin;
    }

    public File getCacheFolder() {
        return cacheFolder;
    }

    @Override
    public boolean remove(int id) {
        URLMap map = idMap.get(id);
        if (map != null) {
            map.enabled = false;
            keyMap.remove(map.getKey());
            String playerName = map.getPlayerName();
            if (playerName != null) {
                playerMap.remove(playerName);
            }
            idMap.remove(id);
            return true;
        }

        return false;
    }

    public boolean isAnimationAllowed() {
        return animationAllowed;
    }

    public void setAnimationAllowed(boolean allowed) {
        animationAllowed = allowed;
    }
}
