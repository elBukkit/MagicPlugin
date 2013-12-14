package com.elmakers.mine.bukkit.utilities;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

public class URLMap {
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
	
	public static URLMap get(Short mapId, String url, int x, int y, int width, int height) {
		String key = getKey(url, x, y, width, height);
		if (keyMap.containsKey(key)) {
			return keyMap.get(key);
		}
		
		URLMap newMap = new URLMap(mapId, url, x, y, width, height);
		keyMap.put(key, newMap);
		if (mapId != null) {
			idMap.put(mapId, newMap);
		}
		return newMap;
	}

	public static URLMap get(String url, int x, int y, int width, int height) {
		return get(null, url, x, y, width, height);
	}
	
	public static void resend(String playerName) {
		for (URLMap map : keyMap.values()) {
			map.resendTo(playerName);
		}
	}
	
	public static void resetAll() {
		for (URLMap map : keyMap.values()) {
			map.reset();
		}
	}
	
	public static void save(ConfigurationSection root) {
		for (URLMap map : keyMap.values()) {
			ConfigurationSection mapConfig = root.createSection(Short.toString(map.getId()));
			mapConfig.set("url", map.url);
			mapConfig.set("x", map.x);
			mapConfig.set("y", map.y);
			mapConfig.set("width", map.width);
			mapConfig.set("height", map.height);
		}
	}
	
	private URLMap(Short mapId, String url, int x, int y, int width, int height) {
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
	
	public String getKey() {
		return getKey(url, x, y, width, height);
	}
	
	public Short getId() {
		return id;
	}
	
	public void setId(Short id) {
		if (this.id == id) return;
		if (this.id != null) {
			idMap.remove(this.id);
		}
		this.id = id;
		if (this.id != null) {
			idMap.put(this.id, this);
		}
	}
	
	public void resendTo(String playerName) {
		sentToPlayers.remove(playerName);
	}
	
	public void reload() {
		sentToPlayers.clear();
		rendered = false;
		loading = false;
		image = null;
	}

	public BufferedImage getImage(final Plugin plugin) {
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
						ex.printStackTrace();
					}
				}
			});
			return null;
		}
		return image;
	}
	
	public void reset() {
		image = null;
		rendered = false;
		loading = false;
		sentToPlayers.clear();
	}
	
	public boolean isRendered() {
		return rendered;
	}
	
	public void render() {
		rendered = true;
	}
	
	public void sendToPlayer(Player player, MapView mapView) {
		String playerName = player.getName();
		if (!sentToPlayers.contains(playerName)) {
			sentToPlayers.add(playerName);
			player.sendMap(mapView);
		}
	}
}