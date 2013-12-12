package com.elmakers.mine.bukkit.utilities;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

public class SkinRenderer extends MapRenderer {

	private final String playerName;
	private BufferedImage portraitImage;
	private static HashMap<String, BufferedImage> playerImages = new HashMap<String, BufferedImage>();
	
	public SkinRenderer(String playerName) {
		this.playerName = playerName;
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
	
	@Override
	public void render(MapView map, MapCanvas canvas, Player player) {
		if (portraitImage == null) {
			loadSkin();
		}
		if (portraitImage != null) {
			canvas.drawImage(0, 0, portraitImage);
			canvas.drawText(2, 116, MinecraftFont.Font, "¤" + MapPalette.GRAY_2 + ";" + playerName);
			canvas.drawText(3, 117, MinecraftFont.Font, "¤" + MapPalette.WHITE + ";" + playerName);
		}
	}
}
