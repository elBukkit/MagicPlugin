package com.elmakers.mine.bukkit.utility;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

@Immutable
public final class ColorHD {
    private static long BYTES_PER_COMPONENT = 6;
    private static long BITS_PER_COMPONENT = BYTES_PER_COMPONENT * 8;
    private static long COMPONENT_SHIFT = BITS_PER_COMPONENT - 8;
    private static long BIT_MASK = (1L << BITS_PER_COMPONENT) - 1;
    private static final int[] components = new int[3];

    private static Map<String, Color> colorMap;

    private final long red;
    private final long green;
    private final long blue;

    private final Color color;

    public ColorHD(long r, long g, long b) {
        red = r & BIT_MASK;
        blue = b & BIT_MASK;
        green = g & BIT_MASK;
        color = createColor();
    }

    public ColorHD(Color color) {
        this(color.getRed() << COMPONENT_SHIFT,
                color.getGreen() << COMPONENT_SHIFT,
                color.getBlue() << COMPONENT_SHIFT);
    }

    public ColorHD(ConfigurationSection configuration) {
        if (configuration.contains("r")) {
            red = (long)configuration.getInt("r") << COMPONENT_SHIFT;
            green = (long)configuration.getInt("g") << COMPONENT_SHIFT;
            blue = (long)configuration.getInt("b") << COMPONENT_SHIFT;
        } else if (configuration.contains("h")) {
            float h = (float)configuration.getDouble("h");
            float s = (float)configuration.getDouble("s");
            float v = (float)configuration.getDouble("v", configuration.getDouble("b"));
            int[] colors = convertHSBtoRGB(h, s, v);
            red = (long)(colors[0] & 0xFF) << COMPONENT_SHIFT;
            green = (long)(colors[1] & 0xFF) << COMPONENT_SHIFT;
            blue = (long)(colors[2] & 0xFF) << COMPONENT_SHIFT;
        } else {
            red = 0;
            green = 0;
            blue = 0;
        }
        color = createColor();
    }

    public ColorHD(String hexColor) {
        if (hexColor != null && !hexColor.isEmpty() && hexColor.charAt(0) == '#') {
            hexColor = hexColor.substring(1, hexColor.length());
        }
        if (hexColor == null || hexColor.length() == 0) {
            red = 0;
            green = 0;
            blue = 0;
        } else if (hexColor.equals("random")) {
            red =  (long)(Math.random() * BIT_MASK);
            green =  (long)(Math.random() * BIT_MASK);
            blue =  (long)(Math.random() * BIT_MASK);
        } else if (hexColor.length() > 6 && hexColor.contains(",")) {
            String[] pieces = StringUtils.split(hexColor, ',');
            long r = 0;
            long g = 0;
            long b = 0;
            if (pieces.length == 3) {
                try {
                    r = Long.parseLong(pieces[0], 16);
                    g = Long.parseLong(pieces[1], 16);
                    b = Long.parseLong(pieces[2], 16);
                } catch (Exception ignored) {
                }
            }

            red = r;
            green = g;
            blue = b;
        } else {
            Color namedColor = getColorByName(hexColor);
            if (namedColor != null) {
                red = (long)namedColor.getRed() << COMPONENT_SHIFT;
                blue = (long)namedColor.getBlue() << COMPONENT_SHIFT;
                green = (long)namedColor.getGreen() << COMPONENT_SHIFT;
            } else {
                long effectColor = 0;
                try {
                    effectColor = Integer.parseInt(hexColor, 16);
                } catch (Exception ignored) {
                }

                red = ((effectColor >> 16) & 0xFF) << COMPONENT_SHIFT;
                green = ((effectColor >> 8) & 0xFF) << COMPONENT_SHIFT;
                blue = (effectColor & 0xFF) << COMPONENT_SHIFT;
            }
        }
        color = createColor();
    }

    // Borrowed from Sun AWT Color class
    public static int[] convertHSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
            case 0:
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) (t * 255.0f + 0.5f);
                b = (int) (p * 255.0f + 0.5f);
                break;
            case 1:
                r = (int) (q * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) (p * 255.0f + 0.5f);
                break;
            case 2:
                r = (int) (p * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) (t * 255.0f + 0.5f);
                break;
            case 3:
                r = (int) (p * 255.0f + 0.5f);
                g = (int) (q * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
                break;
            case 4:
                r = (int) (t * 255.0f + 0.5f);
                g = (int) (p * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
                break;
            case 5:
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) (p * 255.0f + 0.5f);
                b = (int) (q * 255.0f + 0.5f);
                break;
            }
        }
        components[0] = r;
        components[1] = g;
        components[2] = b;
        return components;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return Long.toHexString(red) + "," + Long.toHexString(green) + "," + Long.toHexString(blue);
    }

    public ColorHD mixColor(long r, long g, long b, double weight) {
        double totalWeight = weight + 1;
        double totalRed = (red + (r * weight));
        double totalGreen = (green + (g * weight));
        double totalBlue = (blue + (b * weight));
        return new ColorHD(
                (long)(totalRed / totalWeight),
                (long)(totalGreen / totalWeight),
                (long)(totalBlue / totalWeight)
            );
    }

    public ColorHD mixColor(ColorHD other, double weight) {
        return mixColor(other.red, other.green, other.blue, weight);
    }

    public ColorHD mixColor(Color other, double weight) {
        if (other == null) return this;
        return mixColor(color.getRed() << COMPONENT_SHIFT,
                color.getGreen() << COMPONENT_SHIFT,
                color.getBlue() << COMPONENT_SHIFT,
                weight);
    }

    public static double getDistance(Color c1, Color c2) {
        double rmean = (c1.getRed() + c2.getRed()) / 2.0;
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        double weightR = 2 + rmean / 256.0;
        double weightG = 4.0;
        double weightB = 2 + (255 - rmean) / 256.0;
        return weightR * r * r + weightG * g * g + weightB * b * b;
    }

    private static Color getColorByName(String name) {
        if (colorMap == null) {
            colorMap = new HashMap<>();
            colorMap.put("WHITE", Color.fromRGB(16777215));
            colorMap.put("SILVER", Color.fromRGB(12632256));
            colorMap.put("GRAY", Color.fromRGB(8421504));
            colorMap.put("BLACK", Color.fromRGB(0));
            colorMap.put("RED", Color.fromRGB(16711680));
            colorMap.put("MAROON", Color.fromRGB(8388608));
            colorMap.put("YELLOW", Color.fromRGB(16776960));
            colorMap.put("OLIVE", Color.fromRGB(8421376));
            colorMap.put("LIME", Color.fromRGB(65280));
            colorMap.put("GREEN", Color.fromRGB(32768));
            colorMap.put("AQUA", Color.fromRGB(65535));
            colorMap.put("TEAL", Color.fromRGB(32896));
            colorMap.put("BLUE", Color.fromRGB(255));
            colorMap.put("NAVY", Color.fromRGB(128));
            colorMap.put("FUCHSIA", Color.fromRGB(16711935));
            colorMap.put("PURPLE", Color.fromRGB(8388736));
            colorMap.put("ORANGE", Color.fromRGB(16753920));
        }

        return colorMap.get(name.toUpperCase());
    }

    private Color createColor() {
        Color testCreate = null;
        try {
            testCreate = Color.fromRGB((int)(red >> COMPONENT_SHIFT), (int)(green >> COMPONENT_SHIFT), (int)(blue >> COMPONENT_SHIFT));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (testCreate == null) {
            testCreate = Color.BLACK;
        }
        return testCreate;
    }
}
