package com.elmakers.mine.bukkit.utilities;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;
import org.bukkit.plugin.Plugin;

public class SkinRenderer extends MapRenderer {

	private final String playerName;
	private BufferedImage portraitImage;
	private static Plugin plugin;
	private static HashMap<String, BufferedImage> playerImages = new HashMap<String, BufferedImage>();
	private static Map<String, Short> playerPortraitIds = new HashMap<String, Short>();
	private static Map<Short, String> portraitIdPlayers = new HashMap<Short, String>();
	private static Set<Short> rendered = new HashSet<Short>();
	private static Set<Short> loading = new HashSet<Short>();

	@SuppressWarnings("deprecation")
	public static void loadPlayers(Plugin callingPlugin, Map<String, Short> playerMapIds) {
		plugin = callingPlugin;
		if (playerMapIds == null) return;
		
		playerPortraitIds.clear();
		portraitIdPlayers.clear();
		for (Entry<String, Short> entry : playerMapIds.entrySet()) {
			MapView playerMap = Bukkit.getMap(entry.getValue());
			if (playerMap != null) {
				for(MapRenderer renderer : playerMap.getRenderers()) {
					playerMap.removeRenderer(renderer);
				}
				MapRenderer renderer = new SkinRenderer(entry.getKey());
				playerMap.addRenderer(renderer);
				portraitIdPlayers.put(entry.getValue(), entry.getKey());
				playerPortraitIds.put(entry.getKey(), entry.getValue());
			} else {
				plugin.getLogger().warning("Failed to load map id " + entry.getValue() + " for " + entry.getKey());
			}
		}
	}
	
	public SkinRenderer(String playerName) {
		this.playerName = playerName;
	}
	
	public static String getPlayerName(short mapId) {
		if (!portraitIdPlayers.containsKey(mapId)) {
			return null;
		}
		
		return portraitIdPlayers.get(mapId);
	}
	
	@SuppressWarnings("deprecation")
	public static MapView getPlayerPortrait(String playerName) {
		// We're going to always take maps from the main world
		// This is to avoid potential problems with maps created in other worlds...
		// But it all has the potential to get out of sync anyway.
		World world = Bukkit.getWorlds().get(0);
		
		MapView playerMap = null;
		if (playerPortraitIds.containsKey(playerName)) {
			playerMap = Bukkit.getMap(playerPortraitIds.get(playerName));
		} 

		if (playerMap == null) {
			// Remove old entry ... since apparently this map is broken?
			if (playerPortraitIds.containsKey(playerName)) {
				Short id = playerPortraitIds.get(playerName);
				plugin.getLogger().info("Unregistering map id " + id + " from " + playerName + ", map failed to load");
				portraitIdPlayers.remove(id);
				playerPortraitIds.remove(playerName);
			}
			playerMap = Bukkit.createMap(world);
			if (playerMap != null) {
				plugin.getLogger().info("Created new map id " + playerMap.getId() + " for " + playerName);
				playerPortraitIds.put(playerName, playerMap.getId());
				portraitIdPlayers.put(playerMap.getId(), playerName);
			}
		}
		
		if (playerMap == null) return null;
		
		for (MapRenderer renderer : playerMap.getRenderers()) {
			playerMap.removeRenderer(renderer);
		}
		MapRenderer renderer = new SkinRenderer(playerName);
		playerMap.addRenderer(renderer);
		
		return playerMap;
	}
	
	protected void loadSkin() {
		boolean needLoading = false;
		synchronized(playerImages) {
			if (playerImages.containsKey(playerName)){
				portraitImage = playerImages.get(playerName);
			} else if (plugin != null) {
				portraitImage = null;
				playerImages.put(playerName, null);
				needLoading = true;
			}
		}
		if (needLoading) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					try {
						String skinUrl = "http://s3.amazonaws.com/MinecraftSkins/" + playerName + ".png";
						plugin.getLogger().info("Loading " + skinUrl);
						URL url = new URL(skinUrl);
						BufferedImage skinImage = ImageIO.read(url);
						BufferedImage headImage = skinImage.getSubimage(8, 8, 8, 8);
						portraitImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
					    Graphics2D graphics = portraitImage.createGraphics();
					    AffineTransform transform = AffineTransform.getScaleInstance(16, 16);
					    graphics.drawRenderedImage(headImage, transform);				    
					    synchronized(playerImages) {
					    	playerImages.put(playerName, portraitImage);
					    }
					} catch (Exception ex) {
						portraitImage = null;
						ex.printStackTrace();
					}
				}
			});
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MapView map, MapCanvas canvas, Player player) {
		if (rendered.contains(map.getId())) return;
		
		if (portraitImage == null) {
			loadSkin();
		}
		if (portraitImage != null) {
			canvas.drawImage(0, 0, portraitImage);
			rendered.add(map.getId());
		} else {
			if (!loading.contains(map.getId())) {
				canvas.drawText(2, 116, MinecraftFont.Font, "¤" + MapPalette.GRAY_2 + ";" + playerName);
				canvas.drawText(3, 117, MinecraftFont.Font, "¤" + MapPalette.WHITE + ";" + playerName);
				loading.add(map.getId());
			}
		}
	}
}
