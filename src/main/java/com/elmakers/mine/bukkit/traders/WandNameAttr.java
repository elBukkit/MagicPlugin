package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Name", key="wand_name", priority = 5)
public class WandNameAttr extends NBTItemAttr {
	
	public WandNameAttr(String key) {
		super(key, "wand", "name");
	}
}
