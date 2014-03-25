package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Physical Protection", key="wand_protection_physical", priority = 5)
public class WandProtectionPhysicalAttr extends FloatItemAttr {
	
	public WandProtectionPhysicalAttr(String key) {
		super(key, "wand", "protection_physical");
	}
}
