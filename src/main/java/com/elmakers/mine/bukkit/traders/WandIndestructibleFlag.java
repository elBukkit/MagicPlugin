package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Indestructible", key=".wand_indestructible", priority = 5)
public class WandIndestructibleFlag extends NBTItemFlag {

	public WandIndestructibleFlag(String key) {
		super(key, "wand", "indestructible");
	}
}
