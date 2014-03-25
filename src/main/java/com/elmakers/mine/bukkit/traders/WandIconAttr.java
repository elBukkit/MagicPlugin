package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Icon", key="wand_icon", priority = 5)
public class WandIconAttr extends NBTItemAttr {
	
	public WandIconAttr(String key) {
		super(key, "wand", "icon");
	}
}
