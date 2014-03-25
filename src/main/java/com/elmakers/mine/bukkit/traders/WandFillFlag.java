package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Auto-Fill", key=".wand_fill", priority = 5)
public class WandFillFlag extends NBTItemFlag {
	
	public WandFillFlag(String key) {
		super(key, "wand", "fill");
	}
}
