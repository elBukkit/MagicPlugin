package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Randomize Flag", key=".wand_randomize", priority = 5)
public class WandRandomizeFlag extends NBTItemFlag {

	public WandRandomizeFlag(String key) {
		super(key, "wand", "randomize");
	}
}
