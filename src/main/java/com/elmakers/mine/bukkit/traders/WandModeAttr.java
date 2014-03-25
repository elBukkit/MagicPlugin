package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Inventory Mode", key="wand_mode", priority = 5)
public class WandModeAttr extends NBTItemAttr {
	
	public WandModeAttr(String key) {
		super(key, "wand", "mode");
	}
}
