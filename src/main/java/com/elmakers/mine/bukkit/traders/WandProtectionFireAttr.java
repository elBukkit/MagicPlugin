package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Fire Protection", key="wand_protection_fire", priority = 5)
public class WandProtectionFireAttr extends FloatItemAttr {
	
	public WandProtectionFireAttr(String key) {
		super(key, "wand", "protection_fire");
	}
}
