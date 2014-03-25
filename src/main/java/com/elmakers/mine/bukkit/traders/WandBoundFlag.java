package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Bound Status", key=".wand_bound", priority = 5)
public class WandBoundFlag extends NBTItemFlag {
	
	public WandBoundFlag(String key) {
		super(key, "wand", "bound");
	}
}
