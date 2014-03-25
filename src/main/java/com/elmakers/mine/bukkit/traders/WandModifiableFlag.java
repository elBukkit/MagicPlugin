package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Modifiable Flag", key="wand_modifiable", priority = 5)
public class WandModifiableFlag extends NBTItemFlag {
	
	public WandModifiableFlag(String key) {
		super(key, "wand", "modifiable");
	}
}
