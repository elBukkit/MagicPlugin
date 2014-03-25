package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.utils.items.Attribute;

@Attribute(name="Wand Active Brush", key="wand_active_brush", priority = 5)
public class WandActiveBrushAttr extends NBTItemAttr {
	
	public WandActiveBrushAttr(String key) {
		super(key, "wand", "active_material");
	}
}
