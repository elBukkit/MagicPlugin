package com.elmakers.mine.bukkit.utility;

import javax.annotation.concurrent.Immutable;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

import de.slikey.effectlib.util.ColorUtils;

@Immutable
public final class ColorHD {
    private static long BYTES_PER_COMPONENT = 6;
    private static long BITS_PER_COMPONENT = BYTES_PER_COMPONENT * 8;
    private static long COMPONENT_SHIFT = BITS_PER_COMPONENT - 8;
    private static long BIT_MASK = (1L << BITS_PER_COMPONENT) - 1;
    private static final int[] components = new int[3];

    private final long red;
    private final long green;
    private final long blue;
    private final long alpha;

    private final Color color;

    public ColorHD(long r, long g, long b) {
        this(255, r, g, b);
    }

    public ColorHD(long a, long r, long g, long b) {
        alpha = a & BIT_MASK;
        red = r & BIT_MASK;
        blue = b & BIT_MASK;
        green = g & BIT_MASK;
        color = createColor();
    }

    public ColorHD(Color color) {
        this(ColorUtils.getAlpha(color),
                color.getRed() << COMPONENT_SHIFT,
                color.getGreen() << COMPONENT_SHIFT,
                color.getBlue() << COMPONENT_SHIFT);
    }

    public ColorHD(ConfigurationSection configuration) {
        if (configuration.contains("r")) {
            red = (long)configuration.getInt("r") << COMPONENT_SHIFT;
            green = (long)configuration.getInt("g") << COMPONENT_SHIFT;
            blue = (long)configuration.getInt("b") << COMPONENT_SHIFT;
            alpha = (long)configuration.getInt("a", 255) << COMPONENT_SHIFT;
        } else if (configuration.contains("h")) {
            float h = (float)configuration.getDouble("h");
            float s = (float)configuration.getDouble("s");
            float v = (float)configuration.getDouble("v", configuration.getDouble("b"));
            int[] colors = convertHSBtoRGB(h, s, v);
            red = (long)(colors[0] & 0xFF) << COMPONENT_SHIFT;
            green = (long)(colors[1] & 0xFF) << COMPONENT_SHIFT;
            blue = (long)(colors[2] & 0xFF) << COMPONENT_SHIFT;
            alpha = (long)(0xFF) << COMPONENT_SHIFT;
        } else {
            red = 0;
            green = 0;
            blue = 0;
            alpha = 255;
        }
        color = createColor();
    }

    public ColorHD(String hexColor) {
        if (hexColor != null && hexColor.length() > 6 && hexColor.contains(",")) {
            // This is a special format used to serialized the full range color
            String[] pieces = StringUtils.split(hexColor, ',');
            long r = 0;
            long g = 0;
            long b = 0;
            long a = 255;
            if (pieces.length == 3) {
                try {
                    r = Long.parseLong(pieces[0], 16);
                    g = Long.parseLong(pieces[1], 16);
                    b = Long.parseLong(pieces[2], 16);
                } catch (Exception ignored) {
                }
            }
            if (pieces.length == 4) {
                try {
                    a = Long.parseLong(pieces[3], 16);
                } catch (Exception ignored) {
                }
            }

            red = r;
            green = g;
            blue = b;
            alpha = a;
        } else {
            Color color = ColorUtils.parse(hexColor);
            if (color == null) {
                red = 0;
                green = 0;
                blue = 0;
                alpha = 255;
            } else {
                red = color.getRed() << COMPONENT_SHIFT;
                green = color.getGreen() << COMPONENT_SHIFT;
                blue = color.getBlue() << COMPONENT_SHIFT;
                alpha = color.getAlpha() << COMPONENT_SHIFT;
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
        return Long.toHexString(red) + "," + Long.toHexString(green) + "," + Long.toHexString(blue) + "," + Long.toHexString(alpha);
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

    private Color createColor() {
        return ColorUtils.fromARGB((int)(alpha >> COMPONENT_SHIFT), (int)(red >> COMPONENT_SHIFT), (int)(green >> COMPONENT_SHIFT), (int)(blue >> COMPONENT_SHIFT), Color.BLACK);
    }
}
