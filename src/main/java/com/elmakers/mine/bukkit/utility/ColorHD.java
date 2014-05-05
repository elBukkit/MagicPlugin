package com.elmakers.mine.bukkit.utility;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;

public class ColorHD implements Cloneable {
	private static long BYTES_PER_COMPONENT = 6;
	private static long BITS_PER_COMPONENT = BYTES_PER_COMPONENT * 8;
	private static long COMPONENT_SHIFT = BITS_PER_COMPONENT - 8;
	private static long BIT_MASK = (1l << BITS_PER_COMPONENT) - 1;
	
	private final long red;
	private final long green;
	private final long blue;
	
	private final Color color;
	
	public ColorHD(long r, long g, long b) {
		red = r & BIT_MASK;
		blue = b & BIT_MASK;
		green = g & BIT_MASK;
		Color testCreate = null;
		try {
			testCreate = Color.fromRGB((int)(red >> COMPONENT_SHIFT), (int)(green >> COMPONENT_SHIFT), (int)(blue >> COMPONENT_SHIFT));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		color = testCreate;
	}
	
	public ColorHD(Color color) {
		this(color.getRed() << COMPONENT_SHIFT,
				color.getGreen() << COMPONENT_SHIFT,
				color.getBlue() << COMPONENT_SHIFT);
	}
	
	public ColorHD(String hexColor) {
		if (hexColor == null || hexColor.length() == 0) {
			red = 0;
			green = 0;
			blue = 0;
		} else if (hexColor.equals("random")) {
			red =  (long)(Math.random() * (double)BIT_MASK);
			green =  (long)(Math.random() * (double)BIT_MASK);
			blue =  (long)(Math.random() * (double)BIT_MASK);
		} else if (hexColor.length() > 6 && hexColor.contains(",")) {
			String[] pieces = StringUtils.split(hexColor, ",");
			long r = 0;
			long g = 0;
			long b = 0;
			if (pieces.length == 3) {
				try {
					r = Long.parseLong(pieces[0], 16);
					g = Long.parseLong(pieces[1], 16);
					b = Long.parseLong(pieces[2], 16);
				} catch (Exception ex) {
					
				}
			}
			red = r;
			green = g;
			blue = b;
		} else {
			long effectColor = 0;
			try {
				effectColor = Integer.parseInt(hexColor, 16);
			} catch (Exception ex) {
				
			}
			red = ((effectColor >> 16) & 0xFF) << COMPONENT_SHIFT;
			green = ((effectColor >> 8) & 0xFF) << COMPONENT_SHIFT;
			blue = ((effectColor) & 0xFF) << COMPONENT_SHIFT;
		}
		Color testCreate = null;
		try {
			testCreate = Color.fromRGB((int)(red >> COMPONENT_SHIFT), (int)(green >> COMPONENT_SHIFT), (int)(blue >> COMPONENT_SHIFT));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		color = testCreate;
	}
	
	public Color getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return Long.toHexString(red) + "," + Long.toHexString(green) + "," + Long.toHexString(blue);
	}
	
	@Override
	public Object clone() {
		return new ColorHD(red, green, blue);
	}
	
	public ColorHD mixColor(long r, long g, long b, double weight) {
		double totalWeight = weight + 1;
		double totalRed = ((double)red + ((double)r * weight));
		double totalGreen = ((double)green + ((double)g * weight));
		double totalBlue = ((double)blue + ((double)b * weight));
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
}
