package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Brushes", key="wand_brushes", priority = 5)
public class WandBrushListAttr extends NBTItemAttr {
	
	public WandBrushListAttr(String key) {
		super(key, "wand", "materials");
	}
}
