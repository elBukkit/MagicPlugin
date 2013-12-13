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

import com.elmakers.mine.bukkit.plugins.magic.PlayerSpells;
import com.elmakers.mine.bukkit.plugins.magic.Spells;

public class SkinRenderer extends MapRenderer {

	private final String playerName;
	private BufferedImage portraitImage;
	private static HashMap<String, BufferedImage> playerImages = new HashMap<String, BufferedImage>();
	private static Map<String, Short> playerPortraitIds = new HashMap<String, Short>();
	private static Map<Short, String> portraitIdPlayers = new HashMap<Short, String>();
	private static Set<Short> rendered = new HashSet<Short>();

	@SuppressWarnings("deprecation")
	public static void loadPlayers(Map<String, Short> playerMapIds) {
		playerPortraitIds = playerMapIds;
		portraitIdPlayers.clear();
		for (Entry<String, Short> entry : playerMapIds.entrySet()) {
			MapView playerMap = Bukkit.getMap(entry.getValue());
			for(MapRenderer renderer : playerMap.getRenderers()) {
				playerMap.removeRenderer(renderer);
			}
			MapRenderer renderer = new SkinRenderer(entry.getKey());
			playerMap.addRenderer(renderer);
			portraitIdPlayers.put(entry.getValue(), entry.getKey());
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
		} else {
			playerMap = Bukkit.createMap(world);
			playerPortraitIds.put(playerName, playerMap.getId());
			portraitIdPlayers.put(playerMap.getId(), playerName);
		}
		
		for (MapRenderer renderer : playerMap.getRenderers()) {
			playerMap.removeRenderer(renderer);
		}
		MapRenderer renderer = new SkinRenderer(playerName);
		playerMap.addRenderer(renderer);
		
		return playerMap;
	}
	
	// Magic-specific version of this function for tracking player/map id associations
	@SuppressWarnings("deprecation")
	public static MapView getPlayerPortrait(String playerName, Spells spells) {
		MapView mapView = getPlayerPortrait(playerName);
		PlayerSpells playerSpells = spells.getPlayerSpells(playerName);
		Short currentId = playerSpells.getPortraitMapId();
		if (currentId == null || currentId != mapView.getId()) {
			playerSpells.setPortraitMapId(mapView.getId());
			spells.save();
		}
		
		return mapView;
	}
	
	protected void loadSkin() {
		if (playerImages.containsKey(playerName)){
			portraitImage = playerImages.get(playerName);
		}
		try {
			URL url = new URL("http://s3.amazonaws.com/MinecraftSkins/" + playerName + ".png");
			BufferedImage skinImage = ImageIO.read(url);
			BufferedImage headImage = skinImage.getSubimage(8, 8, 8, 8);
			portraitImage = new BufferedImage(128, 128, skinImage.getType());
		    Graphics2D graphics = portraitImage.createGraphics();
		    AffineTransform transform = AffineTransform.getScaleInstance(16, 16);
		    graphics.drawRenderedImage(headImage, transform);
		    playerImages.put(playerName, portraitImage);
		} catch (Exception ex) {
			portraitImage = null;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MapView map, MapCanvas canvas, Player player) {
		if (rendered.contains(map.getId())) return;
		rendered.add(map.getId());
		
		if (portraitImage == null) {
			loadSkin();
		}
		if (portraitImage != null) {
			canvas.drawImage(0, 0, portraitImage);
		} else {
			canvas.drawText(2, 116, MinecraftFont.Font, "¤" + MapPalette.GRAY_2 + ";" + playerName);
			canvas.drawText(3, 117, MinecraftFont.Font, "¤" + MapPalette.WHITE + ";" + playerName);
		}
	}
}
