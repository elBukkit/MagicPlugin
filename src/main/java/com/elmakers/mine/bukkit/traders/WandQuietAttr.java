package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Verbosity", key="wand_quiet", priority = 5)
public class WandQuietAttr extends NBTItemAttr {
	
	public WandQuietAttr(String key) {
		super(key, "wand", "quiet");
	}
}
