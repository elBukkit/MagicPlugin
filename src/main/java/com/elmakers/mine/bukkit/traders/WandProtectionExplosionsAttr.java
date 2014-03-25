package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Explosion Protection", key="wand_protection_explosions", priority = 5)
public class WandProtectionExplosionsAttr extends FloatItemAttr {
	
	public WandProtectionExplosionsAttr(String key) {
		super(key, "wand", "protection_explosions");
	}
}
