package com.elmakers.mine.bukkit.utilities;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	// Map ids will be saved in /plugins/<yourplugin>/<configurationFileName>
	private final static String configurationFileName = "urlmaps.yml";
	
	// This can be used to avoid a big lag hit on login.
	// If you have a lot of maps in one place, try making this number larger.
	// A map has a 1 / staggerSending chance of being sent to a player (that hasn't already received it) every tick.
	private final int staggerSending = 10;
	
	// Public API

	/**
	 * This loads the configuration file and sets up map renderers.
	 * 
	 * This should be called in your plugin's onEnable.
	 * 
	 * @param callingPlugin
	 */
	public static void load(Plugin callingPlugin) {
		plugin = callingPlugin;
		YamlConfiguration configuration = new YamlConfiguration();
		File configurationFile = getConfigurationFile();
		if (configurationFile.exists()) {
			try {
				configuration.load(configurationFile);
				Set<String> maps = configuration.getKeys(false);
				boolean needsUpdate = false;
				for (String mapIdString : maps) {
					ConfigurationSection mapConfig = configuration.getConfigurationSection(mapIdString);
					try {
						Short mapId = null;
						URLMap map = null;
						try {
							mapId = Short.parseShort(mapIdString);
						} catch (Exception ex) {
							map = get(mapConfig.getString("url"), mapConfig.getInt("x"), mapConfig.getInt("y")
								, mapConfig.getInt("width"), mapConfig.getInt("height"));
							plugin.getLogger().info("Created new map id " + map.id + " for config id " + mapIdString);
							needsUpdate = true;
						}
						if (map == null && mapId != null) {
							map = get(mapId, mapConfig.getString("url"), mapConfig.getInt("x"), mapConfig.getInt("y")
									, mapConfig.getInt("width"), mapConfig.getInt("height"));
						}
						
						if (map == null) {
							throw new Exception("Failed to load map id " + mapIdString);
						}

						// Hook up the renderer
						map.getMapView();
					} catch (Exception ex) {
						plugin.getLogger().warning("Failed to load " + configurationFile.getAbsolutePath() + ": " + ex.getMessage());
					}
				}

				if (needsUpdate) {
					save();
				}
			} catch (Exception ex) {
				plugin.getLogger().warning("Failed to load " + configurationFile.getAbsolutePath() + ": " + ex.getMessage());
			}
		}
	}
	
	/**
	 * Saves the configuration file.
	 * 
	 * This is called automatically as changes are made, but you can call it in onDisable to be safe.
	 */
	public static void save() {
		YamlConfiguration configuration = new YamlConfiguration();
		File configurationFile = getConfigurationFile();
		for (URLMap map : idMap.values()) {
			ConfigurationSection mapConfig = configuration.createSection(Short.toString(map.id));
			mapConfig.set("url", map.url);
			mapConfig.set("x", map.x);
			mapConfig.set("y", map.y);
			mapConfig.set("width", map.width);
			mapConfig.set("height", map.height);
		}
		try {
			configuration.save(configurationFile);
		} catch (Exception ex) {
			plugin.getLogger().warning("Failed to save file " + configurationFile.getAbsolutePath());
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

	/**
	 * A helper function to get an ItemStack from a MapView.
	 * 
	 * @param name
	 * 	The display name to give the new item. Optional.
	 * @param mapView
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack getMapItem(String name, MapView mapView) {
		ItemStack newMapItem = new ItemStack(Material.MAP, 1, mapView.getId());
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
	 * @param playerName
	 * @return
	 */
	public static ItemStack getPlayerPortrait(String playerName) {
		MapView mapView = getURL("http://s3.amazonaws.com/MinecraftSkins/" + playerName + ".png", 8, 8, 8, 8);
		return getMapItem("Photo of " + playerName, mapView);
	}

	/**
	 * Force reload of a player headshot.
	 * 
	 * @param playerName
	 */
	public static void forceReloadPlayerPortrait(String playerName) {
		URLMap map = URLMap.get("http://s3.amazonaws.com/MinecraftSkins/" + playerName + ".png", 8, 8, 8, 8);
		map.reload();
	}

	/**
	 * Force reload of the specific url and cropping.
	 * 
	 * @param url
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void forceReload(String url, int x, int y, int width, int height) {
		URLMap.get(url, x, y, width, height).reload();
	}
	
	/**
	 * Get a new MapView for the specified url. 
	 * 
	 * The full image will be used.
	 * 
	 * @param url
	 * @return
	 */
	public static MapView getURL(String url) {
		return getURL(url, 0, 0, 0, 0);
	}

	/**
	 * Get a new ItemStack for the specified url with a specific cropping.
	 * 
	 * @param url
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param name
	 * 	The displayName to give the new item.
	 * @return
	 */
	public static ItemStack getURLItem(String url, int x, int y, int width, int height, String name) {
		MapView mapView = getURL(url, x, y, width, height);
		return getMapItem(name, mapView);
	}

	/**
	 * Get a new MapView for the specified url with a specific cropping.
	 * 
	 * @param url
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static MapView getURL(String url, int x, int y, int width, int height) {
		URLMap map = URLMap.get(url, x, y, width, height);
		return map.getMapView();
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
			sendToPlayer(player, mapView);
			return;
		}
		
		BufferedImage image = getImage();
		if (image != null) {
			canvas.drawImage(0, 0, image);
			rendered = true;
		}
	}
	
	// Private Functions and data
	
	private static Plugin plugin;
	
	private static HashMap<String, URLMap> keyMap = new HashMap<String, URLMap>();
	private static HashMap<Short, URLMap> idMap = new HashMap<Short, URLMap>();
	
	private Short id;
	private BufferedImage image;
	
	protected String url;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected boolean rendered = false;
	protected volatile boolean loading = false;
	protected Set<String> sentToPlayers = new HashSet<String>();
	
	private static URLMap get(short mapId, String url, int x, int y, int width, int height) {
		String key = getKey(url, x, y, width, height);
		URLMap map = idMap.get(mapId);
		if (map != null) {
			if (!map.getKey().equals(key)) {
				plugin.getLogger().warning("Two maps with the same id but different keys: " + mapId + ": " + key + ", " + map.getKey());
			}
			return map;
		}
		map = keyMap.get(key);
		if (map != null) {
			if (map.id != mapId) {
				plugin.getLogger().warning("Two maps with the same key but different ids: " + key + ": " + mapId + ", " + map.id);
			}
			return map;
		}
		
		map = new URLMap(mapId, url, x, y, width, height);
		keyMap.put(key, map);
		idMap.put(mapId, map);
		return map;
	}

	@SuppressWarnings("deprecation")
	private static URLMap get(String url, int x, int y, int width, int height) {
		String key = getKey(url, x, y, width, height);
		if (keyMap.containsKey(key)) {
			return keyMap.get(key);
		}
		World world = Bukkit.getWorlds().get(0);
		MapView mapView = Bukkit.createMap(world);
		if (mapView == null) {
			plugin.getLogger().warning("Unable to create new map for url key " + key);
			return null;
		}
		URLMap newMap = get(mapView.getId(), url, x, y, width, height);
		save();
		return newMap;
	}
	
	@SuppressWarnings("deprecation")
	private MapView getMapView() {
		MapView mapView = Bukkit.getMap(id);
		if (mapView == null) {
			plugin.getLogger().warning("Failed to get map id " + id + " for key " + getKey());
			return null;
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

	private static File getConfigurationFile() {
		File dataFolder = plugin.getDataFolder();
		return new File(dataFolder, configurationFileName);
	}
	
	private URLMap(short mapId, String url, int x, int y, int width, int height) {
		this.url = url;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.id = mapId;
	}
	
	private static String getKey(String url, int x, int y, int width, int height) {
		return "" + x + "," + y + "|" + width + "," + height + "|" + url;
	}
	
	private String getKey() {
		return getKey(url, x, y, width, height);
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
		if (loading) {
			return null;
		}
		if (image == null) {
			loading = true;
			image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					try {
						plugin.getLogger().info("Loading " + url);
						URL connection = new URL(url);
						BufferedImage rawImage = ImageIO.read(connection);
						width = width <= 0 ? rawImage.getWidth() + width : width;
						height = height <= 0 ? rawImage.getHeight() + height : height;
						BufferedImage croppedImage = rawImage.getSubimage(x, y, width, height);
					    Graphics2D graphics = image.createGraphics();
					    AffineTransform transform = AffineTransform.getScaleInstance((float)128 / width, (float)128 / height);
					    graphics.drawRenderedImage(croppedImage, transform);
					    loading = false;
					} catch (Exception ex) {
						plugin.getLogger().warning("Failed to load url " + url + ": " + ex.getMessage());
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
		String playerName = player.getName();
		// Randomly stagger sending to avoid a big hit on login
		if (!sentToPlayers.contains(playerName) && (Math.random() * staggerSending) <= 1) {
			sentToPlayers.add(playerName);
			player.sendMap(mapView);
		}
	}
}
