package com.elmakers.mine.bukkit.maps;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapFont;
import org.bukkit.map.MapFont.CharacterSprite;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

import com.elmakers.mine.bukkit.utility.ColorHD;

/**
 * This is a MapCanvas implementation that can be used
 * to retrieve the contents of a Map.
 *
 * <p>It can be queried for raw data, and can also do
 * translation to DyeColor values.
 *
 */
public class BufferedMapCanvas implements MapCanvas {

    public static int CANVAS_WIDTH = 128;
    public static int CANVAS_HEIGHT = 128;

    private static MapCursorCollection emptyCursors = new MapCursorCollection();
    private byte[] pixels = new byte[128 * 128];
    private Map<Byte, DyeColor> dyeColors = new HashMap<>();

    @Nullable
    @Override
    public MapView getMapView() {
        return null;
    }

    @Override
    public MapCursorCollection getCursors() {
        return emptyCursors;
    }

    @Override
    public void setCursors(MapCursorCollection cursors) {
        // .. Nothing.
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setPixel(int x, int y, byte color) {
        if (x < 0 || y < 0 || x > CANVAS_WIDTH || y > CANVAS_HEIGHT) return;

        pixels[x + y * CANVAS_WIDTH] = color;

        // Map colors in advance.
        if (color != MapPalette.TRANSPARENT && !dyeColors.containsKey(color)) {
            java.awt.Color mapColor = MapPalette.getColor(color);
            Color targetColor = Color.fromRGB(mapColor.getRed(), mapColor.getGreen(), mapColor.getBlue());

            // Find best dyeColor
            DyeColor bestDyeColor = null;
            Double bestDistance = null;
            for (DyeColor testDyeColor : DyeColor.values()) {
                Color testColor = testDyeColor.getColor();
                double testDistance = ColorHD.getDistance(testColor, targetColor);
                if (bestDistance == null || testDistance < bestDistance) {
                    bestDistance = testDistance;
                    bestDyeColor = testDyeColor;
                    if (testDistance == 0) break;
                }
            }

            dyeColors.put(color, bestDyeColor);
        }
    }

    @Override
    public byte getPixel(int x, int y) {
        if (x < 0 || y < 0 || x > CANVAS_WIDTH || y > CANVAS_HEIGHT) return 0;

        return pixels[x + y * CANVAS_WIDTH];
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public DyeColor getDyeColor(int x, int y) {
        byte color = getPixel(x, y);
        if (color == MapPalette.TRANSPARENT) return null;
        if (!dyeColors.containsKey(color)) return null;

        return dyeColors.get(color);
    }

    @Override
    public byte getBasePixel(int x, int y) {
        return 0;
    }

    // Shamelessly stolen from CraftMapCanvas.... wish they'd give us
    // an extendible version or just let us create them at least :)
    @Override
    @SuppressWarnings("deprecation")
    public void drawImage(int x, int y, Image image) {
        byte[] bytes = MapPalette.imageToBytes(image);
        for (int x2 = 0; x2 < image.getWidth(null); ++x2) {
            for (int y2 = 0; y2 < image.getHeight(null); ++y2) {
                setPixel(x + x2, y + y2, bytes[y2 * image.getWidth(null) + x2]);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void drawText(int x, int y, MapFont font, String text) {
        int xStart = x;
        byte color = MapPalette.DARK_GRAY;
        if (!font.isValid(text)) {
            throw new IllegalArgumentException("text contains invalid characters");
        }

        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                x = xStart;
                y += font.getHeight() + 1;
                continue;
            } else if (ch == ChatColor.COLOR_CHAR) {
                int j = text.indexOf(';', i);
                if (j >= 0) {
                    try {
                        color = Byte.parseByte(text.substring(i + 1, j));
                        i = j;
                        continue;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            CharacterSprite sprite = font.getChar(text.charAt(i));
            for (int r = 0; r < font.getHeight(); ++r) {
                for (int c = 0; c < sprite.getWidth(); ++c) {
                    if (sprite.get(r, c)) {
                        setPixel(x + c, y + r, color);
                    }
                }
            }
            x += sprite.getWidth() + 1;
        }
    }

}
