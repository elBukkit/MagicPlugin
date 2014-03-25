package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Fall Protection", key="wand_protection_falling", priority = 5)
public class WandProtectionFallingAttr extends NBTItemAttr {
	
	public WandProtectionFallingAttr(String key) {
		super(key, "wand", "protection_falling");
	}
}
