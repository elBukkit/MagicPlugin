package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Unique Id", key="wand_id", priority = 5)
public class WandIdAttr extends NBTItemAttr {
	
	public WandIdAttr(String key) {
		super(key, "wand", "id", true);
	}
}
