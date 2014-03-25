package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Projectile Protection", key="wand_projectile_protection", priority = 5)
public class WandProtectionProjectilesAttr extends FloatItemAttr {
	
	public WandProtectionProjectilesAttr(String key) {
		super(key, "wand", "protection_projectiles");
	}
}
