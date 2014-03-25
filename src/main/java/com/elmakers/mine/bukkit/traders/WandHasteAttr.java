package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Haste", key="wand_haste", priority = 5)
public class WandHasteAttr extends NBTItemAttr {
	
	public WandHasteAttr(String key) {
		super(key, "wand", "haste");
	}
}
