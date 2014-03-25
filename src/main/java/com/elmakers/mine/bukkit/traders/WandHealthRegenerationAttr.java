package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Health Regeneration", key="wand_health_regeneration", priority = 5)
public class WandHealthRegenerationAttr extends NBTItemAttr {
	
	public WandHealthRegenerationAttr(String key) {
		super(key, "wand", "health_regeneration");
	}
}
