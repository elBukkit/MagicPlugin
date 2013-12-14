package com.elmakers.mine.bukkit.utilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;

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

public class URLMapRenderer extends MapRenderer 
{
	private final static String configurationFileName = "urlmaps.yml";
	
	private static Plugin plugin;
	private static YamlConfiguration configuration;

	private final URLMap map;
	
	protected static File getConfigurationFile() {
		File dataFolder = plugin.getDataFolder();
		return new File(dataFolder, configurationFileName);
	}
	
	@SuppressWarnings("deprecation")
	public static void load(Plugin callingPlugin) {
		plugin = callingPlugin;
		configuration = new YamlConfiguration();
		File configurationFile = getConfigurationFile();
		if (configurationFile.exists()) {
			try {
				configuration.load(configurationFile);
				Set<String> maps = configuration.getKeys(false);
				for (String mapId : maps) {
					ConfigurationSection mapConfig = configuration.getConfigurationSection(mapId);
					try {
						URLMap newMap = URLMap.get(Short.parseShort(mapId), mapConfig.getString("url"), mapConfig.getInt("x"), mapConfig.getInt("y")
								, mapConfig.getInt("width"), mapConfig.getInt("height"));
						
						MapView playerMap = Bukkit.getMap(newMap.getId());
						if (playerMap == null) {
							throw new Exception("Failed to load map id " + newMap.getId() + " for url key " + newMap.getKey());
						}
						for(MapRenderer renderer : playerMap.getRenderers()) {
							playerMap.removeRenderer(renderer);
						}
						MapRenderer renderer = new URLMapRenderer(newMap);
						playerMap.addRenderer(renderer);
					} catch (Exception ex) {
						plugin.getLogger().warning("Failed to load " + configurationFile.getAbsolutePath() + ": " + ex.getMessage());
					}
				}
			} catch (Exception ex) {
				plugin.getLogger().warning("Failed to load " + configurationFile.getAbsolutePath() + ": " + ex.getMessage());
			}
		}
	}
	
	public static void save() {
		if (configuration == null) {
			configuration = new YamlConfiguration();
		}
		File configurationFile = getConfigurationFile();
		URLMap.save(configuration);
		try {
			configuration.save(configurationFile);
		} catch (Exception ex) {
			plugin.getLogger().warning("Failed to save file " + configurationFile.getAbsolutePath());
		}
	}
	
	private URLMapRenderer(URLMap urlMap) {
		map = urlMap;
	}
	
	public URLMapRenderer(String url, int x, int y, int width, int height) {
		this(URLMap.get(url, x, y, width, height));
	}

	@SuppressWarnings("deprecation")
	public static ItemStack getMapItem(String name, MapView mapView) {
		ItemStack newMapItem = new ItemStack(Material.MAP, 1, mapView.getId());
		ItemMeta meta = newMapItem.getItemMeta();
		meta.setDisplayName(name);
		newMapItem.setItemMeta(meta);
		return newMapItem;
	}
	
	public static ItemStack getPlayerPortrait(String playerName) {
		MapView mapView = getURL("http://s3.amazonaws.com/MinecraftSkins/" + playerName + ".png", 8, 8, 8, 8);
		return getMapItem("Photo of " + playerName, mapView);
	}

	public static void forceReloadPlayerPortrait(String playerName) {
		URLMap map = URLMap.get("http://s3.amazonaws.com/MinecraftSkins/" + playerName + ".png", 8, 8, 8, 8);
		map.reload();
	}
	
	public static MapView getURL(String url) {
		return getURL(url, 0, 0, 0, 0);
	}

	public static ItemStack getURLItem(String url, int x, int y, int width, int height, String name) {
		MapView mapView = getURL(url, x, y, width, height);
		return getMapItem(name, mapView);
	}
	
	@SuppressWarnings("deprecation")
	public static MapView getURL(String url, int x, int y, int width, int height) {
		// We're going to always take maps from the main world
		// This is to avoid potential problems with maps created in other worlds...
		// But it all has the potential to get out of sync anyway.
		World world = Bukkit.getWorlds().get(0);
		MapView mapView = null;
		URLMap map = URLMap.get(url, x, y, width, height);
		if (map.getId() != null) {
			mapView = Bukkit.getMap(map.getId());
			plugin.getLogger().warning("Failed to get map id " + map.getId() + ", unregistering from key " + map.getKey());
			map.setId(null);
		}
		
		if (map.getId() == null) {
			mapView = Bukkit.createMap(world);
			if (mapView == null) {
				plugin.getLogger().warning("Failed to create a new map");
			} else {
				map.setId(mapView.getId());
			}
		}
		if (mapView == null) {
			return mapView;
		}
		
		for (MapRenderer renderer : mapView.getRenderers()) {
			mapView.removeRenderer(renderer);
		}
		MapRenderer renderer = new URLMapRenderer(map);
		mapView.addRenderer(renderer);
		
		return mapView;
	}
	
	public static void resend(String playerName) {
		URLMap.resend(playerName);
	}
	
	public static void forceReload(String url, int x, int y, int width, int height) {
		URLMap.get(url, x, y, width, height).reload();
	}
	
	@Override
	public void render(MapView mapView, MapCanvas canvas, Player player) {
		if (map.isRendered()) {
			map.sendToPlayer(player, mapView);
			return;
		}
		
		BufferedImage image = map.getImage(plugin);
		if (image != null) {
			canvas.drawImage(0, 0, image);
			map.render();
		}
	}
}
