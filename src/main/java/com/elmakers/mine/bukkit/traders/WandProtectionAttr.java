package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Overall Protection", key="wand_protection", priority = 5)
public class WandProtectionAttr extends NBTItemAttr {
	
	public WandProtectionAttr(String key) {
		super(key, "wand", "protection");
	}
}
