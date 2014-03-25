package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Keep on Death", key=".wand_keep", priority = 5)
public class WandKeepFlag extends NBTItemFlag {
	
	public WandKeepFlag(String key) {
		super(key, "wand", "keep");
	}
}
