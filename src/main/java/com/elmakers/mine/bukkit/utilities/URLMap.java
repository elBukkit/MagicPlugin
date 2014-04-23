package com.elmakers.mine.bukkit.utilities;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

public class URLMap extends MapRenderer  {
	private static File configurationFile = null;
	private static File cacheFolder = null;
	private static boolean disabled = false;
	
	// Public API
	
	public static Set<Entry<Short, URLMap>> getAll() {
		return idMap.entrySet();
	}

	public static void loadConfiguration() {
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
	}
	
	/**
	 * This loads the configuration file and sets up map renderers.
	 * 
	 * This should be called in your plugin's onEnable.
	 * 
	 * @param callingPlugin
	 * @param configFile
	 * @param cache
	 */
	public static void load(Plugin callingPlugin, File configFile, File cache) {
		cacheFolder = cache;
		plugin = callingPlugin;
		configurationFile = configFile;
		loadConfiguration();
	}
	
	/**
	 * Saves the configuration file.
	 * 
	 * This is called automatically as changes are made, but you can call it in onDisable to be safe.
	 */
	public static void save() {
		if (configurationFile == null || disabled) return;
		
		YamlConfiguration configuration = new YamlConfiguration();
		for (URLMap map : idMap.values()) {
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
		try {
			configuration.save(configurationFile);
		} catch (Exception ex) {
			warning("Failed to save file " + configurationFile.getAbsolutePath());
		}
	}
	
	private static void info(String message)
	{
		if (plugin != null){
			plugin.getLogger().info(message);
		}
	}
	
	private static void warning(String message)
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
	public static void resetAll() {
		for (URLMap map : keyMap.values()) {
			map.reset();
		}
	}
	
	public static void clearCache() {
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
	public static ItemStack getMapItem(String name, short mapId) {
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
	 * @param photoName
	 * @return
	 */
	public static ItemStack getPlayerPortrait(String worldName, String playerName, Integer priority, String photoName) {
		photoName = photoName == null ? playerName : photoName;
		String photoLabel = "Photo of " + photoName;
		MapView mapView = getURL(worldName, "http://s3.amazonaws.com/MinecraftSkins/" + playerName + ".png", photoLabel, 8, 8, 40, 8, 8, 8, priority);
		return getMapItem(photoLabel, mapView);
	}

	public static MapView getURL(String worldName, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
		URLMap map = URLMap.get(worldName, url, name, x, y, xOverlay, yOverlay, width, height, priority);
		return map.getMapView();
	}
	
	/**
	 * Get a new ItemStack for the specified url with a specific cropping.
	 * 
	 */
	public static ItemStack getURLItem(String world, String url, String name, int x, int y, int width, int height, Integer priority) {
		MapView mapView = getURL(world, url, name, x, y, null, null, width, height, priority);
		return getMapItem(name, mapView);
	}
	
	@SuppressWarnings("deprecation")
	protected static ItemStack getMapItem(String name, MapView mapView) {
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
	public static void forceReloadPlayerPortrait(String worldName, String playerName) {
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
	public static void forceReload(String worldName, String url, int x, int y, int width, int height) {
		URLMap.get(worldName, url, x, y, width, height).reload();
	}
	
	/**
	 * Force resending all maps to a specific player.
	 * @param playerName
	 */
	public static void resend(String playerName) {
		for (URLMap map : keyMap.values()) {
			map.resendTo(playerName);
		}
	}
	
	// Render method override
	@Override
	public void render(MapView mapView, MapCanvas canvas, Player player) {
		if (rendered) {
			if (priority != null) {
				sendToPlayer(player, mapView);
			}
			return;
		}
		
		BufferedImage image = getImage();
		if (image != null) {
			canvas.drawImage(0, 0, image);
			rendered = true;
		}
	}
	
	@Override
	public void initialize(MapView mapView) {
		// This is here mainly as a hack to be able to force render to canvas.
		rendered = false;
	}
	
	public boolean matches(String keyword) {
		if (keyword == null || keyword.length() == 0) return true;
		
		String lowerUrl = url == null ? "" : url.toLowerCase();
		String lowerName = name == null ? "" : name.toLowerCase();
		String lowerKeyword = keyword.toLowerCase();
		return lowerUrl.contains(lowerKeyword) || lowerName.contains(lowerKeyword);
	}
	
	public String getName() {
		return name;
	}
	
	public String getURL() {
		return url;
	}
	
	// Private Functions and data
	
	private static Plugin plugin;
	
	private static HashMap<String, URLMap> keyMap = new HashMap<String, URLMap>();
	private static HashMap<Short, URLMap> idMap = new HashMap<Short, URLMap>();
	
	private String world;
	private Short id;
	private BufferedImage image;
	
	protected String url;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected Integer xOverlay;
	protected Integer yOverlay;
	protected String name;
	protected boolean enabled = true;
	protected boolean rendered = false;
	protected volatile boolean loading = false;
	protected Set<String> sentToPlayers = new HashSet<String>();
	protected Integer priority;
	
	private static URLMap get(String world, short mapId, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
		String key = getKey(world, url, x, y, width, height);
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
		
		map = new URLMap(world, mapId, url, name, x, y, xOverlay, yOverlay, width, height, priority);
		keyMap.put(key, map);
		idMap.put(mapId, map);
		return map;
	}

	private static URLMap get(String worldName, String url, int x, int y, int width, int height) {
		return get(worldName, url, x, y, width, height, null);
	}
	
	@SuppressWarnings("deprecation")
	private static URLMap get(String worldName, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
		String key = getKey(worldName, url, x, y, width, height);
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
	
	private static URLMap get(String worldName, String url, int x, int y, int width, int height, Integer priority) {
		return get(worldName, url, null, x, y, null, null, width, height, priority);
	}
	
	private MapView getMapView() {
		return getMapView(true);
	}
	
	@SuppressWarnings("deprecation")
	private MapView getMapView(boolean recreateIfNecessary) {
		if (!enabled) {
			return null;
		}
		MapView mapView = Bukkit.getMap(id);
		if (mapView == null) {
			keyMap.remove(getKey());
			enabled = false;
			warning("Failed to get map id " + id + " for key " + getKey() + ", disabled, re-enable in config and fix id");
			save();
			return mapView;
		}
		List<MapRenderer> renderers = mapView.getRenderers();
		boolean needsRenderer = false;
		for (MapRenderer renderer : renderers) {
			if (!(renderer instanceof URLMap)) {
				mapView.removeRenderer(renderer);
				needsRenderer = true;
			}
		}
		if (needsRenderer) {
			mapView.addRenderer(this);
		}
		return mapView;
	}
	
	private void disable() {
		enabled = false;
	}
	
	private boolean isEnabled() {
		return enabled;
	}
	
	private URLMap(String world, short mapId, String url, String name, int x, int y, Integer xOverlay, Integer yOverlay, int width, int height, Integer priority) {
		this.world = world;
		this.url = url;
		this.name = name;
		this.x = x;
		this.y = y;
		this.xOverlay = xOverlay;
		this.yOverlay = yOverlay;
		this.width = width;
		this.height = height;
		this.id = mapId;
		this.priority = priority;
	}
	
	private static String getKey(String world, String url, int x, int y, int width, int height) {
		return world + "|" + x + "," + y + "|" + width + "," + height + "|" + url;
	}
	
	private String getKey() {
		return getKey(world, url, x, y, width, height);
	}
	
	private void resendTo(String playerName) {
		sentToPlayers.remove(playerName);
	}
	
	private void reload() {
		sentToPlayers.clear();
		rendered = false;
		loading = false;
		image = null;
	}

	private BufferedImage getImage() {
		if (loading || !enabled) {
			return null;
		}
		if (image == null) {
			loading = true;
			image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
			if (plugin == null) return null;
			
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					try {
						BufferedImage rawImage = null;
						@SuppressWarnings("deprecation")
						String cacheFileName = URLEncoder.encode(url);
						File cacheFile = cacheFolder != null ? new File(cacheFolder, cacheFileName) : null;
						if (cacheFile != null) {
							if (cacheFile.exists()) {
								info("Loading from cache: " + cacheFile.getName());
								rawImage = ImageIO.read(cacheFile);
							} else {
								info("Loading " + url);
								URL imageUrl = new URL(url);
								HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
					            conn.setConnectTimeout(30000);
					            conn.setReadTimeout(30000);
					            conn.setInstanceFollowRedirects(true);
					            InputStream in = conn.getInputStream();
					            OutputStream out = new FileOutputStream(cacheFile);
					            byte[] buffer = new byte[10 * 1024];
					            int len;
					            while ((len = in.read(buffer)) != -1) {
					                out.write(buffer, 0, len);
					            }
					            out.close();
					            in.close();
					            
					            rawImage = ImageIO.read(cacheFile);
							}
						} else {
							info("Loading " + url);
							URL imageUrl = new URL(url);
							rawImage = ImageIO.read(imageUrl);
						}
						
						width = width <= 0 ? rawImage.getWidth() + width : width;
						height = height <= 0 ? rawImage.getHeight() + height : height;
						BufferedImage croppedImage = rawImage.getSubimage(x, y, width, height);
					    Graphics2D graphics = image.createGraphics();
					    AffineTransform transform = AffineTransform.getScaleInstance((float)128 / width, (float)128 / height);
					    graphics.drawRenderedImage(croppedImage, transform);
					    
					    if (xOverlay != null && yOverlay != null) {
					    	BufferedImage croppedOverlay = rawImage.getSubimage(xOverlay, yOverlay, width, height);
						    graphics.drawRenderedImage(croppedOverlay, transform);
					    }
					    
					    loading = false;
					} catch (Exception ex) {
						warning("Failed to load url " + url + ": " + ex.getMessage());
					}
				}
			});
			return null;
		}
		return image;
	}
	
	private void reset() {
		image = null;
		rendered = false;
		loading = false;
		sentToPlayers.clear();
	}
	
	private void sendToPlayer(Player player, MapView mapView) {
		// Safety check
		if (priority == null || !enabled) {
			return;
		}

		String playerName = player.getName();
		
		// Randomly stagger sending to avoid a big hit on login
		if (!sentToPlayers.contains(playerName) && (Math.random() * priority) <= 1) {
			sentToPlayers.add(playerName);
			player.sendMap(mapView);
		}
	}
	
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
}
