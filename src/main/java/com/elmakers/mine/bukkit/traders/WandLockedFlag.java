package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Locked Flag", key=".wand_locked", priority = 5)
public class WandLockedFlag extends NBTItemFlag {
	
	public WandLockedFlag(String key) {
		super(key, "wand", "locked");
	}
}
